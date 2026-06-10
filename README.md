# 로봇 임대관리시스템 - 3조

백엔드 프레임워크 프로그래밍 팀 프로젝트
웨어러블 로봇 디바이스 임대관리 시스템

## 기술 스택

- Java 21 + Spring Boot 4.0.6 + JPA + Thymeleaf
- MySQL 8.0 (원격 서버)
- Gradle 9.4.1 (Wrapper)
- MQTT (DrValue 라이브러리)
- JWT 인증 (Access/Refresh Token) + 역할 기반 접근제어(RBAC)

## DB 접속 정보

| 항목 | 값 |
|------|-----|
| Host | 101.79.16.88 |
| Port | 3306 |
| Database | team3 |
| User | team3 |
| Password | 팀 내부 채널로 별도 공유 (보안상 README 미기재) |

```
mysql -h 101.79.16.88 -u team3 -p team3
```

## 실행 방법

1. 프로젝트 clone
```
git clone https://github.com/BackendFrameworkProgramming/RentalManagementSystem.git
```

2. IntelliJ에서 열기 -> Gradle 빌드 대기

3. `RentalManagementSystemApplication.java` 실행

4. 브라우저에서 `http://localhost:8083/login` 접속

> **Java 21 필요.** 로컬에 JDK 21이 없어도 `settings.gradle`의 foojay-resolver 플러그인이
> Gradle 빌드 시 JDK 21을 자동으로 내려받습니다. (단, `java -jar` 직접 실행에는 JDK 21 런타임 필요)

## 환경변수 (보안 설정)

민감 정보와 운영 설정은 환경변수로 주입할 수 있습니다. **설정하지 않으면 안전한 기본값**으로 동작하므로
로컬 실행 시 별도 설정 없이 그대로 돌아갑니다. 운영 서버에서는 아래 값을 환경변수로 주입하세요.

| 환경변수 | 용도 | 기본값 |
|----------|------|--------|
| `DB_USERNAME` | DB 계정 | `team3` |
| `DB_PASSWORD` | DB 비밀번호 | (코드 기본값 — **운영 시 반드시 주입 권장**) |
| `JWT_SECRET` | JWT 서명 키 | (코드 기본값 — **운영 시 반드시 주입 권장**) |
| `JPA_SHOW_SQL` | SQL 콘솔 출력 | `false` (로컬 디버깅 시 `true`) |
| `JPA_FORMAT_SQL` | SQL 포맷 출력 | `false` |
| `LOG_SQL_LEVEL` | Hibernate SQL 로그 레벨 | `WARN` |
| `LOG_APP_LEVEL` | 애플리케이션 로그 레벨 | `INFO` |

> ⚠️ 현재 DB 비밀번호/JWT 시크릿의 기본값은 과거 git 히스토리에 노출된 적이 있습니다.
> 실제 운영 보안을 위해서는 **비밀번호 교체 + 환경변수 주입**이 필요합니다.

예) 로컬에서 SQL 보면서 디버깅:
```bash
JPA_SHOW_SQL=true ./gradlew bootRun
```

## 보안 (OWASP Top 10 대응)

수업에서 다룬 OWASP Top 10 항목을 코드에 반영했습니다.

- **A01 접근 통제**: 회원가입(계정 생성)은 ADMIN만 가능. 관리 화면/API는 역할 기반 접근제어(RBAC) 적용
- **A02 암호화 실패**: 비밀번호는 BCrypt 단방향 해시. DB 비번/JWT 시크릿은 환경변수로 외부화
- **A05 보안 설정 오류**: SQL/상세 로그 운영 기본값 off (내부 구조 노출 방지)
- **A06 취약 구성요소**: 의존성 버전은 Spring BOM으로 고정(결정적 빌드)
- **A07 인증 실패**: 로그인 5회 실패 시 5분 일시 잠금 (`LoginAttemptService`, 무차별 대입 방어)
- **A09/A10 로깅·예외 처리**: 예외 응답에는 일반 메시지만, 상세(스택 트레이스)는 서버 로그/DB에만 기록

### 향후 보안 과제 (TODO)

아직 처리하지 않았거나 추가 검토가 필요한 항목입니다.

- [ ] **A01 접근 통제 — 회원가입 정책 재검토**: 현재 회원가입을 ADMIN 전용으로 제한했으나,
      시연/운영 시나리오에 따라 일반 가입 허용 여부를 다시 결정할 것. 더불어 본인 리소스만
      접근 가능한지(IDOR) 메소드 단위 권한 검증도 함께 검토.
- [ ] **DB 비밀번호 / JWT 시크릿 교체**: 환경변수로 외부화는 했으나 기존 값이 git 히스토리에
      남아 있음. 근본 해결을 위해 실제 비밀번호/시크릿을 교체할 것.
- [ ] **의존성 취약점 스캔 (A06)**: OWASP dependency-check를 별도 CI 환경에서 실행해
      취약 라이브러리 점검. (Gradle 플러그인 직접 적용은 의존성 충돌로 보류)
- [ ] **배포 전 보안 점검 절차화**: 런칭 전 보안 점검 툴 1회 실행 + AI 코드 검증을 거쳐 배포하는
      절차를 팀 규칙으로 정착. (교수님 강조 사항)
      → HTTPS 적용 완료 후 점검 도구 선정: **Nikto**(서버 CLI),
      **securityheaders.com / CryptCheck**(웹). `:8083` 비표준 포트라 SSL Labs·Mozilla
      Observatory(443 전용)는 사용 불가.

## 보안 점검 (배포 전 스캔)

배포 전 1회 보안 점검 — 교수님 강조 사항. 점검 대상: **https://rms.o-r.kr:8083**

> `:8083` 비표준 포트라 SSL Labs·Mozilla Observatory(443 전용)는 사용 불가 → 아래 3종으로 대체.

각자 본인 도구를 실행하고 **표에서 본인 행만** 갱신(상태 `⬜ 예정` → `✅ 완료`, 결과 요약 기입)한 뒤
커밋한다. 결과 화면 캡처는 제출물/노션에 첨부.

| 도구 | 점검 항목 | 담당 | 상태 | 결과 요약 |
|------|----------|------|------|----------|
| Nikto | 웹서버 취약점·설정오류 | 김규민 | ⬜ 예정 | |
| securityheaders.com | 보안 HTTP 헤더 등급 | 정은혜 | ⬜ 예정 | |
| CryptCheck.fr | TLS 등급 | 전민석 | ⬜ 예정 | |

**실행 방법**

```bash
# 김규민 — Nikto (서버 SSH)
sudo apt install -y nikto
nikto -h https://rms.o-r.kr:8083
```
- 정은혜 — securityheaders.com 접속 후 입력칸에 `https://rms.o-r.kr:8083` → Scan
- 전민석 — CryptCheck.fr 접속 후 입력칸에 `rms.o-r.kr:8083` → 검사

> 헤더/TLS 등급이 낮게(B~C) 나오면 **nginx 설정**으로 보강(HSTS·CSP·Referrer-Policy·
> Permissions-Policy, TLS 프로토콜/cipher)해 A로 올린다. 앱 코드가 아니라 nginx 한 곳에서 수정.

## 배포 (Deployment)

운영 서버에 배포되어 있습니다.

- **접속 주소 (HTTPS)**: https://rms.o-r.kr:8083/login
- **서버 환경**: Naver Cloud Platform (Ubuntu 24.04), OpenJDK 21

> 기존 `http://101.79.16.88:8083` 직접 접속은 현재 nginx가 8083을 TLS로 점유하므로
> 더 이상 평문 HTTP로 열리지 않습니다. **위 HTTPS 도메인으로 접속하세요.**

### HTTPS / TLS 구성 (2026-06-10 적용)

ACG(클라우드 방화벽)가 80/443을 막고 8083만 열려 있어, **이미 열린 8083을 nginx가 TLS로
점유**하고 실제 Spring 앱은 내부 포트(9083)로 옮기는 방식으로 HTTPS를 적용했다.

```
[브라우저] --HTTPS--> nginx (0.0.0.0:8083, TLS 종료) --HTTP--> Spring 앱 (127.0.0.1:9083)
```

| 구성 | 값 |
|------|-----|
| 도메인 | `rms.o-r.kr` (내도메인.한국 무료 도메인, A레코드 → 101.79.16.88) |
| 인증서 | Let's Encrypt — **DNS-01 수동 인증** (HTTP-01은 80포트 미개방으로 불가) |
| 인증서 경로 | `/etc/letsencrypt/live/rms.o-r.kr/` |
| 만료일 | **2026-09-07** (수동 발급 — 자동 갱신 안 됨, 갱신 시 certbot 명령 재실행) |
| nginx 설정 | `/etc/nginx/sites-available/rms.o-r.kr` (8083 ssl → 127.0.0.1:9083) |
| 앱 포트 | `127.0.0.1:9083` (`config/application.properties` 오버라이드) |
| 프로세스 관리 | PM2 (`name: team_3`, `id 2`) — `pm2 restart team_3` |

#### 서버 포트 오버라이드 — `git pull` 안전

nginx 뒤에서 앱은 9083으로 떠야 하지만, **추적되는** `src/main/resources/application.properties`는
레포 원본(`8083`) 그대로 둔다. 서버 전용 설정은 **추적되지 않는** `config/application.properties`에
두어, Spring Boot가 classpath 설정보다 **우선** 로드하게 한다:

```properties
# ~/team/team3/RentalManagementSystem/config/application.properties  (서버 전용, git 추적 안 됨)
server.port=9083
server.address=127.0.0.1
server.forward-headers-strategy=framework
```

이 파일은 `.git/info/exclude`에 `/config/`로 등록돼 커밋되지 않으므로, 서버에서 `git pull` 해도
**충돌·원복이 없다.** 배포는 평소대로:

```bash
git pull origin main && pm2 restart team_3
```

### 서버 SSH 접속

Windows PowerShell 또는 터미널에서:

```
ssh root@101.79.16.88
```

- 비밀번호 입력 시 화면에 아무것도 안 보이는 게 정상입니다. 그대로 입력 후 Enter.
- 서버 IP / 계정 / 비밀번호 등 접속 정보는 팀 내부 채널로 별도 공유합니다. (보안상 미기재)

### 서버에서 배포/갱신 절차

```bash
# 1. 프로젝트 디렉터리로 이동
cd ~/team/team3/RentalManagementSystem

# 2. 최신 코드 받기
git pull origin main

# 3. 빌드 (테스트 제외)
./gradlew clean build -x test

# 4. 백그라운드로 실행 (SSH 종료 후에도 유지)
nohup java -jar build/libs/RentalManagementSystem-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 실행 확인
ss -lntp | grep 8083      # 포트 점유 확인
tail -f app.log           # 로그 확인 (Started ... 메시지 후 Ctrl+C로 빠져나오기)

# 종료할 때
ps -ef | grep RentalManagement   # PID 확인 후
kill <PID>
```

> **트러블슈팅** — `git pull` 시 `gradle-wrapper.jar would be overwritten` 에러가 나면,
> 추적되지 않는 jar이 남아있는 경우입니다. `rm gradle/wrapper/gradle-wrapper.jar` 후 다시 `git pull` 하세요.

## 기본 계정

앱 최초 실행 시 관리자 계정이 자동 생성됩니다 (`DataInitializer`).

- 아이디: `admin`
- 비밀번호: 팀 내부 채널로 공유 (소스 `DataInitializer.java` 참고)

> 일반 회원 비밀번호는 단방향 암호화되어 저장되므로 복호화로 조회할 수 없습니다.
> 잊은 경우 재가입하거나 DB에서 해시를 갱신해야 합니다.

## 담당 배정

| 담당 | 화면 | 패키지       |
|------|------|-----------|
| 윤태웅 (PM) | 화면1 디바이스 현황 + 모델 관리 + 화면6 센터정보 + 에러로그 + 공통코드 + 사용자 + MQTT | taewoong/ |
| 팀원1 전민석 | 화면5 지점 관리 + 화면7 부서/팀 | minseok/  |
| 팀원2 정은혜 | 화면8 센터 담당직원 + 화면3 생체정보/응급 | Eunhye/   |
| 팀원3 김규민 | 화면2 임대 현황 + 화면4 AS 관리 | Gyumin/   |

## 프로젝트 구조

```
src/main/java/hanyang/RentalManagementSystem/
  common/           <- Entity, Repository, DTO, 공통 구조 (PM)
  taewoong/         <- 디바이스, 모델, 센터, 코드, 사용자, MQTT, 에러로그
  minseok/          <- 지점, 부서/팀 (전민석)
  Eunhye/          <- 직원, 생체/응급 (정은혜)
  Gyumin/          <- 임대, AS (김규민)

src/main/resources/
  templates/        <- Thymeleaf HTML
  static/           <- CSS, JS
  application.properties
```

## Git 규칙

- 본인 폴더(minseok, Eunhye, Gyumin)에서만 작업
- common/ 폴더 수정 금지
- 작업 전 pull, 작업 후 commit -> pull -> push

```
git pull origin main
git add .
git commit -m "작업내용"
git pull origin main
git push origin main
```

## 변경 이력

### 2026-06-10 — HTTPS(TLS) 적용 및 보안 점검 환경 구축

- **무료 도메인 발급**: `rms.o-r.kr` (내도메인.한국, A레코드 → 101.79.16.88)
- **Let's Encrypt 인증서 발급**: DNS-01 수동 인증 (ACG 80/443 미개방으로 HTTP-01 불가)
- **nginx 리버스 프록시 구성**: 8083을 nginx가 TLS 종료 → 내부 `127.0.0.1:9083` 앱으로 전달
- **앱 포트 이동**: 8083 → 9083 (추적 안 되는 `config/application.properties`로 오버라이드 → `git pull` 안전)
- **리다이렉트 정상화**: `X-Forwarded-Host/Port` 헤더 추가로 프록시 뒤 스킴/포트 유실 해결
- **접속 주소**: https://rms.o-r.kr:8083/login
- 자세한 구성·주의사항은 [배포 > HTTPS / TLS 구성](#https--tls-구성-2026-06-10-적용) 참고

### 2026-05-30 — Java 21 업그레이드 및 서버 배포

- **Java 17 → 21 업그레이드** (`build.gradle` toolchain 버전 상향)
- **Gradle toolchain 자동 조달**: `settings.gradle`에 foojay-resolver 플러그인 추가 → JDK 21 자동 다운로드
- **gradle-wrapper.jar 추가**: git에 누락돼 있던 wrapper jar 커밋 (clone 후 `./gradlew` 실행 가능)
- **서버 포트 8080 → 8083 변경** (`application.properties`)
- **NCP Ubuntu 서버 배포**: clone → 빌드 → 실행, http://101.79.16.88:8083 구동 확인
