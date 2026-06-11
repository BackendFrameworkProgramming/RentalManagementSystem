# 팀원 잔여 과제 (2026-06-12, PM 윤태웅 구조 리팩토링 후)

이번에 PM(윤태웅)이 공통 구조(`common/`)와 4개 도메인 전반을 리팩토링하면서
**교수님 최종 피드백 5종**(① 페이징→필터 순서 ② findAll→count ③ 카운트 쿼리 ④ Map→DTO ⑤ status/role Enum)을
대부분 반영했습니다. 다만 **각자 본인 도메인에서 교수님 피드백 항목 1개씩은 직접 마무리**하도록 남겨두었습니다.
(참고: PM이 동일 패턴을 다른 도메인에 이미 적용해두었으니 그대로 따라가면 됩니다.)

---

## 김규민 (gyumin) — AsRecord 상태 Enum화 (피드백 #5)
- **위치**: `common/entity/AsRecord.java`(status 필드), `gyumin/service/AsRecordService.java`, `gyumin/dto/AsRecordResponse.java`
- **할 일**: `AsRecord.status`(String)를 `AsStatus` enum으로 전환.
  - `common/enums/RentalStatus.java`(PM이 작성)를 그대로 본떠 `common/enums/AsStatus.java` 생성: `AS_RECEIVED, AS_PROGRESS, AS_COMPLETED`.
  - 엔티티에 `@Enumerated(EnumType.STRING)` 적용, 서비스의 문자열 비교/전이 검증을 enum으로 교체.
- **⚠ 주의(데이터 정리)**: 기존 DB `as_record.status`에 코드와 안 맞는 **`COMPLETED`(2건)** 가 있음(코드는 `AS_COMPLETED` 사용).
  enum 전환 전 `UPDATE as_record SET status='AS_COMPLETED' WHERE status='COMPLETED';` 로 정리해야 조회 시 매핑 오류가 안 남.
- **참고**: PM이 한 `RentalStatus` / `RentalService.validateStatusTransition(RentalStatus)` 방식 그대로.

## 정은혜 (eunhye) — Employee 상태 Enum화 (피드백 #5)
- **위치**: `common/entity/Employee.java`(employmentType/workStatus), `eunhye/service/EmployeeService.java`, `eunhye/dto/EmployeeResponse.java`·`EmployeeUpsertRequest.java`
- **할 일**: 두 필드를 enum으로 전환.
  - `common/enums/EmploymentType.java`: `FULL_TIME, CONTRACT`
  - `common/enums/WorkStatus.java`: `WORKING, LEAVE`
  - (DB 기존값과 일치하므로 데이터 정리 불필요) `@Enumerated(EnumType.STRING)` 적용.
- **참고**: PM이 한 `Device.status`(DeviceStatus) 적용 방식 그대로.

## 전민석 (minseok) — 부서 삭제 체크를 count/exists 쿼리로 (피드백 #3)
- **위치**: `minseok/service/DepartmentService.java`의 `deleteDepartment()`
- **할 일**: `teamRepository.findAllByDepartmentIdAndIsDeletedFalse(id).isEmpty()` (전체 로드)를
  `TeamRepository`에 `boolean existsByDepartmentIdAndIsDeletedFalse(Long departmentId)` 추가 후 `!exists(...)` 로 교체.
  - 이전엔 `common/` 수정 금지라 못 했던 부분 — 이번에 PM이 공통 구조를 열어두었으니 가능.
- **참고**: PM이 한 `EmployeeRepository.existsByTeamIdAndIsDeletedFalse` / `DeviceRepository.count*` 방식 그대로.

---

각 항목 위치에 `// TODO(이름)` 주석도 달아두었습니다. 본인 폴더 + 해당 엔티티만 수정하면 되고,
완료 후 설계이력(라운드11)의 "팀원 담당 잔여 과제"가 모두 처리됩니다.
