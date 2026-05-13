# 로봇 임대관리시스템 - 3조

백엔드 프레임워크 프로그래밍 팀 프로젝트
웨어러블 로봇 디바이스 임대관리 시스템

## 기술 스택

- Java 17 + Spring Boot 4.0.6 + JPA + Thymeleaf
- MySQL 8.0 (원격 서버)
- Gradle
- MQTT (DrValue 라이브러리)

## DB 접속 정보

| 항목 | 값 |
|------|-----|
| Host | 101.79.16.88 |
| Port | 3306 |
| Database | team3 |
| User | team3 |
| Password | zJzIz7ZANNuSjsjRjDxHbTPHg2RylNX8 |

```
mysql -h 101.79.16.88 -u team3 -pzJzIz7ZANNuSjsjRjDxHbTPHg2RylNX8 team3
```

## 실행 방법

1. 프로젝트 clone
```
git clone https://github.com/BackendFrameworkProgramming/RentalManagementSystem.git
```

2. IntelliJ에서 열기 -> Gradle 빌드 대기

3. `RentalManagementSystemApplication.java` 실행

4. 브라우저에서 `http://localhost:8080` 접속

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

