# 로봇 임대관리시스템 — 최종 리팩토링 변경 명세 (라운드11)

> **3조 · 2026-06-12 · PM 윤태웅**
> 교수님 최종 코드리뷰 피드백 5종 + "유저 정의" 지적 반영 + 다른 조 우수사례(EntityGraph) 차용.
> 기존 API 명세서 v3 / 테이블 명세서 v3 대비 **바뀐 부분만** 정리(나머지는 그대로 유효).

---

## 0. 역할(권한) 모델 — **확정**

이 시스템은 **센터 내부 운영 시스템**이다. 로그인 주체는 "운영 측"이고, 디바이스 착용자/신청자는 시스템이 관리하는 **데이터(`rental.user`)** 이지 로그인 주체가 아니다.

| 역할(코드) | 표시 | 접근 범위 |
|------------|------|-----------|
| `ADMIN` | 관리자 | 운영화면 8개 + **관리화면 5개**(모델·공통코드·사용자·에러로그·설계이력) |
| `STAFF` | 운영자 | 운영화면 8개(디바이스·임대·AS·지점·센터·부서팀·직원·생체) 전체 조회/운영 |

- **비관리자 로그인의 기본값 = `STAFF`**. 관리자만 `ADMIN`.
- 데이터 스코핑(본인 것만)은 **적용하지 않음** — 운영자는 전체를 본다.
- 접근통제(OWASP A01)는 **관리화면 5개를 ADMIN 전용**으로 막는 것 + 회원가입/사용자관리 ADMIN 전용으로 구현.
- `Role` enum에는 `USER`(착용자 직접 로그인), `BRANCH_MANAGER`(지점 단위)도 **확장용으로 보존**되어 있고, 해당 역할이 부여되면 본인/본인지점 데이터로 자동 스코핑되도록 코드는 준비돼 있음(현행 운영 범위에선 미사용).
- 교수님 "유저=착용자/신청자 흐름이 빠졌다" 지적 → **데이터 모델**(`rental.user`, `wear_yn`, 신청→수령→착용→반납, 본인 생체/응급)로 충족.

---

## 1. DB 변경 (테이블 명세 v3 → 현재)

### 1-1. `user` 테이블 — 컬럼 추가/정정
| 컬럼 | 변경 | 비고 |
|------|------|------|
| `password` | (명세 v3에 누락돼 있었음) VARCHAR(255) | 라운드9 JWT 때부터 존재. BCrypt 해시 |
| `role` | VARCHAR(20) | 값 운영범위 = `ADMIN`/`STAFF` (기본 STAFF). enum 매핑 |
| `employee_id` | **신규** BIGINT, FK→employee, NULL | (확장) STAFF↔직원 연결용 |
| `branch_id` | **신규** BIGINT, FK→branch, NULL | (확장) 지점관리자 연결용 |

> ddl-auto=update가 부팅 시 `employee_id`/`branch_id` 컬럼을 자동 추가(추가형이라 기존 데이터 안전).

### 1-2. status 컬럼 — Enum 매핑 (컬럼 타입·값 변화 없음)
- `device.status` → `DeviceStatus` enum (`@Enumerated(STRING)`)
- `rental.status` → `RentalStatus` enum
- `as_record.status`, `employee.employment_type`, `employee.work_status` → **아직 String** (조원 carve-out으로 enum 전환 예정)

### 1-3. 관계 추가 (+2)
| 부모 | 자식 | FK |
|------|------|-----|
| employee | user | employee_id (N:1, nullable) |
| branch | user | branch_id (N:1, nullable) |

→ 그 외 테이블 구조/관계는 v3 그대로.

---

## 2. API 변경 (API 명세 v3 → 현재)

### 2-1. 핵심: **엔드포인트·URL·HTTP코드·파라미터·응답필드 = 그대로 유지**
- 내부 구현만 `Map<String,Object>` → **Request/Response DTO**로 전환(교수님 #4). JSON 필드명/형태는 보존했으므로 프론트 영향 없음.
- 일부 응답에서 누락값이 `"-"`(문자열) → `null`로 바뀐 미세 차이만 있음(표시상 빈 칸, 동작 영향 없음).

### 2-2. 동작/파라미터 변경 (정정 필요한 항목)
| 항목 | 변경 |
|------|------|
| 사용자 등록/수정 `POST/PATCH /api/users` | 기존 필드 + `role`(ADMIN/STAFF), `employeeId?`, `branchId?` 수신. **수정(PATCH) 기능 신규**(화면 모달 추가) |
| 회원가입 `POST /api/auth/signup` | ADMIN 전용. 생성 역할 기본 `STAFF` |
| 로그인 응답 / JWT | `branchId` 클레임 추가(확장용) |
| 임대/AS 목록 | 역할 스코핑 코드 존재하나 운영범위(ADMIN/STAFF)에선 **전체 조회**. `USER`/`BRANCH_MANAGER` 부여 시에만 본인/본인지점으로 자동 제한 |
| 사용자별 임대/AS 조회 | 본인 또는 ADMIN/STAFF만 → 아니면 `403 FORBIDDEN`(IDOR 방어, 확장 대비) |

### 2-3. 에러코드 추가
`FORBIDDEN`, `INVALID_STATUS`, `USER_NOT_FOUND`, `DUPLICATE_LOGIN_ID`, `ACCOUNT_LOCKED`, `EMPLOYEE_NOT_FOUND`, `BRANCH_NOT_FOUND`

### 2-4. 내부 품질 개선 (교수님 피드백 5종)
| # | 피드백 | 적용 |
|---|--------|------|
| 1 | 페이징 먼저→필터 | 쿼리 단계(WHERE)에서 필터 후 페이징. 임대/AS 청크 전체스캔 제거 → `@Query` |
| 2 | findAll 후 스트림 | 디바이스 삭제체크·MQTT 디바이스 조회 등 `exists`/직접조회 쿼리로 |
| 3 | 카운트 전체조회 | `count`/`group by` 쿼리 (지점·모델 집계, 모델버전 연결수) |
| 4 | Map→DTO | 전 9개 도메인 Request/Response DTO |
| 5 | status Enum | Role/DeviceStatus/RentalStatus enum화 |
| + | N+1 (다른 조 우수사례) | `@EntityGraph`/JOIN FETCH (디바이스·임대·AS·직원 목록) |

---

## 3. 코드 규칙 변경 (조원 공유)

1. **이제 컨트롤러/서비스에서 `Map<String,Object>` 금지 → DTO 사용.** 본인 도메인 DTO는 `{도메인}/dto/` 에 있음. API 추가 시 `XxxRequest`/`XxxResponse`로.
2. **목록 조회**: `findAll().stream().filter()` 금지 → 리포지토리 쿼리 메서드/`@Query`로 필터·페이징. 카운트는 `count` 쿼리. 연관 조회 N+1은 `@EntityGraph`.
3. **status는 enum**(Device/Rental). 비교·세팅 시 enum 상수 사용.
4. **`common/` 은 PM이 정리함** — 본인 carve-out(아래) 외에는 건드리지 말 것.
5. 본인 폴더(`gyumin`/`eunhye`/`minseok`) + 본인 carve-out 대상 엔티티만 수정.

---

## 4. 조원별 할 일 (각자 교수님 피드백 1개씩 — `TODO-TEAM.md` + 코드 `// TODO(이름)`)

### 김규민 (gyumin) — AsRecord 상태 Enum화 [#5]
- `common/enums/AsStatus.java` 생성: `AS_RECEIVED, AS_PROGRESS, AS_COMPLETED`
- `AsRecord.status`(String) → `@Enumerated(STRING) AsStatus`, `AsRecordService`의 문자열 비교/전이검증 enum으로
- **⚠ 데이터 정리 필수**: 기존 DB에 `as_record.status='COMPLETED'`(2건)가 코드값 `AS_COMPLETED`와 불일치 →
  enum 전환 전 `UPDATE as_record SET status='AS_COMPLETED' WHERE status='COMPLETED';`
- 참고: PM이 한 `RentalStatus` 방식 그대로

### 정은혜 (eunhye) — Employee 상태 Enum화 [#5]
- `common/enums/EmploymentType.java`: `FULL_TIME, CONTRACT`
- `common/enums/WorkStatus.java`: `WORKING, LEAVE`
- `Employee.employmentType`/`workStatus` → enum (DB 기존값 일치, 데이터 정리 불필요)
- 참고: PM이 한 `DeviceStatus` 방식 그대로

### 전민석 (minseok) — 부서 삭제 체크 쿼리화 [#3]
- `TeamRepository`에 `boolean existsByDepartmentIdAndIsDeletedFalse(Long departmentId)` 추가
- `DepartmentService.deleteDepartment()`의 `findAll().isEmpty()` → `!exists(...)` 로 교체
- 참고: PM이 한 `EmployeeRepository.existsByTeamId...` 방식 그대로

---

## 5. 검증 & 배포

### 빌드 게이트 (PR 머지 후 필수)
```bash
git pull origin main
./gradlew clean build      # contextLoads 테스트 = 라이브 DB로 컨텍스트 로드 → JPQL/매핑/enum/DDL 검증
# BUILD SUCCESSFUL 이어야 배포
```

### 배포
```bash
ssh root@101.79.16.88
cd ~/team/team3/RentalManagementSystem
git pull origin main
./gradlew clean build -x test
pm2 restart team_3
```

### 배포 후 스모크 테스트 (https://rms.o-r.kr:8083)
- 로그인(admin / admin123) → 8개 운영화면 + 5개 관리화면 목록 로딩
- 각 도메인 등록/수정/삭제 1건 (DTO 전환 확인)
- 상태 변경(디바이스/임대/AS) 1회씩 (enum)
- 검색/페이징(임대·AS)
- 집계 패널 숫자(지점/모델)
- **사용자 관리**: 등록 / **수정(신규)** / 삭제, 역할 운영자·관리자 표시
- **김규민 작업분**: AS 목록 정상 로딩(= COMPLETED 데이터 정리 됐는지 확인 포인트)
- 설계이력 화면에 **라운드11** 표시
