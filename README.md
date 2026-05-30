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

## 배포 (Deployment)

운영 서버에 배포되어 있습니다.

- **접속 주소**: http://101.79.16.88:8083/login
- **서버 환경**: Naver Cloud Platform (Ubuntu 24.04), OpenJDK 21

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

### 2026-05-30 — Java 21 업그레이드 및 서버 배포

- **Java 17 → 21 업그레이드** (`build.gradle` toolchain 버전 상향)
- **Gradle toolchain 자동 조달**: `settings.gradle`에 foojay-resolver 플러그인 추가 → JDK 21 자동 다운로드
- **gradle-wrapper.jar 추가**: git에 누락돼 있던 wrapper jar 커밋 (clone 후 `./gradlew` 실행 가능)
- **서버 포트 8080 → 8083 변경** (`application.properties`)
- **NCP Ubuntu 서버 배포**: clone → 빌드 → 실행, http://101.79.16.88:8083 구동 확인
