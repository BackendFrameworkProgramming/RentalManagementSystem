package hanyang.RentalManagementSystem.common.config;

import hanyang.RentalManagementSystem.common.entity.DesignHistory;
import hanyang.RentalManagementSystem.common.entity.User;
import hanyang.RentalManagementSystem.common.repository.DesignHistoryRepository;
import hanyang.RentalManagementSystem.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DesignHistoryRepository designHistoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsByUserLoginIdAndIsDeletedFalse("admin")) {
            userRepository.save(User.builder().userLoginId("admin").password(passwordEncoder.encode("admin123"))
                    .userName("관리자").role("ADMIN").email("admin@team3.com").contact("").build());
            log.info("[INIT] 관리자 계정 생성 완료 (admin / admin123)");
        }
        // 라운드별로 누락된 항목만 추가 시드한다(기존 DB에 새 라운드를 반영하기 위함).
        seedDesignHistory();
    }

    private void seedDesignHistory() {
        seed(1,"3/28","교수님 피드백","prof","초기 DB 설계 착수",
            "[{\"label\":\"화면설계서 기반 DB 설계\",\"before\":\"화면설계서 8개 분석\",\"after\":\"테이블 15개, API 51개로 초안 완성\",\"reason\":\"교수님 지시에 따라 초기 설계 착수\"}]");
        seed(2,"4/4","조교님 피드백","ta","공통코드 분리 + Soft Delete 도입",
            "[{\"label\":\"공통코드 분리\",\"before\":\"상태값(입고, 임대중 등)을 각 테이블에 직접 문자열로 저장\",\"after\":\"code_group + code_detail 테이블 분리. 상태값을 공통코드로 관리\",\"reason\":\"조교님: '상태값이나 유형 같은 코드성 데이터는 공통코드 테이블로 빼서 관리해야 한다'\"},"
            +"{\"label\":\"is_deleted 추가\",\"before\":\"삭제 시 DB에서 물리 삭제 (DELETE)\",\"after\":\"모든 주요 테이블에 is_deleted 컬럼 추가. Soft Delete 적용\",\"reason\":\"조교님: '실무에서는 데이터를 물리 삭제하지 않고 논리 삭제한다'\"}]");
        seed(3,"4/11","교수님 + 타조 피드백","prof","모델-버전 분리 + 상태 전이 규칙",
            "[{\"label\":\"model → model + model_version 분리\",\"before\":\"device_model 테이블 하나에 모델명, 버전, 스펙 등이 모두 포함\",\"after\":\"model(기본정보) + model_version(버전별 스펙, 매뉴얼) 2개 테이블로 분리\",\"reason\":\"교수님: '같은 모델이라도 버전이 다르면 스펙이 다르다'\"},"
            +"{\"label\":\"디바이스 상태 전이 규칙 정의\",\"before\":\"디바이스 상태를 자유롭게 변경 가능\",\"after\":\"7개 상태와 허용된 전이만 가능하도록 validateStatusTransition() 추가\",\"reason\":\"타조: '입고에서 바로 임대중으로 넘어가면 안 된다'\"}]");
        seed(4,"4/18","교수님 + 조교 피드백","prof","RESTful URL + 공통 검색/페이징",
            "[{\"label\":\"URL 규칙 통일\",\"before\":\"/device, /getDeviceList 등 제각각\",\"after\":\"RESTful 규칙 적용. GET /api/devices, POST, PATCH, DELETE\",\"reason\":\"교수님: 'URL은 RESTful 규칙을 따라야 한다'\"},"
            +"{\"label\":\"CommonSearchRequest 도입\",\"before\":\"각 API마다 검색/정렬/페이징 파라미터를 개별 정의\",\"after\":\"CommonSearchRequest 클래스로 searchField, searchKeyword, page, size 통일\",\"reason\":\"교수님: '검색 조건은 공통으로 묶어서 일관되게 처리해야 한다'\"},"
            +"{\"label\":\"페이지네이션 적용\",\"before\":\"목록 API가 전체 데이터를 한 번에 반환\",\"after\":\"Pagination 객체로 page, size, totalElements, totalPages 반환\",\"reason\":\"조교님: '데이터가 많아지면 전체를 한 번에 보내면 안 된다'\"}]");
        seed(5,"4/25","교수님 피드백","prof","API 실명제 + 집계 API 분리 + BaseEntity",
            "[{\"label\":\"API 실명제\",\"before\":\"누가 어떤 API를 담당하는지 불명확\",\"after\":\"72개 API에 담당자 명시 (윤태웅 32개, 전민석 18개, 정은혜 12개, 김규민 21개)\",\"reason\":\"교수님: '개인별로 점수화할 것'\"},"
            +"{\"label\":\"집계 API 분리\",\"before\":\"목록 조회 API에서 집계도 같이 처리\",\"after\":\"/api/devices/summary/by-branch 등 집계 전용 API 분리\",\"reason\":\"교수님: '집계는 별도 API로 분리하는 게 맞다'\"},"
            +"{\"label\":\"AOP BaseEntity\",\"before\":\"각 Entity에 createdAt, updatedAt을 수동 관리\",\"after\":\"BaseEntity로 @CreatedDate, @LastModifiedDate 자동 관리\",\"reason\":\"교수님: 'AOP 관점에서 공통 필드는 BaseEntity로 빼라'\"}]");
        seed(6,"5/3","AI 교차 검증","ai","Repository 쿼리 최적화 + 캐스팅 버그 수정",
            "[{\"label\":\"Repository 쿼리 메서드 추가\",\"before\":\"findAll() + stream filter로 데이터 필터링\",\"after\":\"findAllByIsDeletedFalse(pageable) 등 Repository 쿼리 메서드 선언\",\"reason\":\"AI(Claude Opus): 'findAll()로 전체 데이터를 메모리에 올리면 성능 문제'\"},"
            +"{\"label\":\"배치 API 캐스팅 수정\",\"before\":\"(List<Long>) body.get('deviceIds') — ClassCastException\",\"after\":\"((List<?>) ...).stream().map(o -> ((Number) o).longValue()) 안전 변환\",\"reason\":\"AI(Gemini): 'JSON 숫자는 Integer로 파싱된다'\"},"
            +"{\"label\":\"BiometricData is_deleted 추가\",\"before\":\"BiometricData에 is_deleted 필드 없음\",\"after\":\"is_deleted 필드 + findAllByIsDeletedFalse() 추가\",\"reason\":\"AI(Sonnet): '다른 테이블은 전부 is_deleted가 있는데 BiometricData만 없다'\"},"
            +"{\"label\":\"@Transactional 명시\",\"before\":\"팀원 프롬프트에 @Transactional 안내 없음\",\"after\":\"@Transactional(rollbackFor = Exception.class) 명시 지침 추가\",\"reason\":\"AI: 'checked exception 시 롤백이 안 된다'\"}]");
        seed(7,"5/9","조교 + 수업","ta","MQTT 모듈 적용 + 원격 DB 전환",
            "[{\"label\":\"MQTT 모듈 적용\",\"before\":\"생체정보/응급정보를 수동 입력\",\"after\":\"DrValue MQTT 라이브러리 적용. REPORT/EMERGENCY 자동 수신 → DB 저장\",\"reason\":\"교수님: '데이터 받아서 DB에 저장하는 것까지 만들어야 한다'\"},"
            +"{\"label\":\"원격 DB 전환\",\"before\":\"로컬 MySQL(localhost)\",\"after\":\"원격 DB 서버(101.79.16.88:3306/team3)로 전환\",\"reason\":\"교수님: '실시간으로 공유돼야 한다'\"},"
            +"{\"label\":\"NoResourceFoundException 필터링\",\"before\":\"favicon.ico 404도 에러 로그에 기록\",\"after\":\"NoResourceFoundException은 에러 로그 저장 제외\",\"reason\":\"브라우저 자동 요청이 에러 로그를 오염시킴\"}]");
        seed(8,"5/12~18","자체 테스트 + AI 검증","self","미구현 API 보완 + 화면 13개 이슈 수정",
            "[{\"label\":\"DeviceController 상태변경 API 추가\",\"before\":\"PATCH /api/devices/{id}/status 미구현\",\"after\":\"updateStatus() + validateStatusTransition() 검증 추가\",\"reason\":\"Postman 테스트 중 미구현 API 발견\"},"
            +"{\"label\":\"CommonCode 코드그룹-코드상세 분리\",\"before\":\"코드그룹과 코드상세가 하나의 CRUD로 섞임\",\"after\":\"코드그룹 CRUD 4개 + 코드상세 CRUD 4개로 분리\",\"reason\":\"Postman 테스트 중 404 발생\"},"
            +"{\"label\":\"디바이스 화면 13개 이슈 수정\",\"before\":\"n건씩 보기 없음, 집계 미표시 등\",\"after\":\"셀렉트박스, 집계 표시, AS이력 전체 필드, remark 버그 수정\",\"reason\":\"화면설계서 대비 미반영 사항\"}]");
        seed(9,"5/23","교수님 중간발표 피드백","prof","AOP 로깅 + JWT 인증 + 역할 기반 접근제어",
            "[{\"label\":\"AOP — ApiLoggingAspect\",\"before\":\"API 실행 시간 로깅 없음\",\"after\":\"모든 @RestController API [API] MethodName - Xms 로깅\",\"reason\":\"교수님: 'AOP를 적용해서 API 실행 시간을 로깅하라'\"},"
            +"{\"label\":\"JWT 로그인/인증\",\"before\":\"인증 없이 모든 페이지 접근 가능\",\"after\":\"JWT Access/Refresh Token + HttpOnly Cookie + 자동 갱신\",\"reason\":\"다른 조 대비 + 보안 수업 대비\"},"
            +"{\"label\":\"역할 기반 접근제어 (RBAC)\",\"before\":\"모든 사용자가 모든 페이지 접근 가능\",\"after\":\"ADMIN만 관리 페이지 접근. 일반 회원은 운영 화면 8개만\",\"reason\":\"관리/운영 기능 분리 필요\"},"
            +"{\"label\":\"에러 로그 상세조회 개선\",\"before\":\"에러 목록만 조회, 발생 화면 불명확\",\"after\":\"발생 화면 컬러 배지 + Draw 상세(Stack Trace 포함)\",\"reason\":\"교수님: '에러 코드를 명세화하라'\"}]");
        seed(10,"5/30","교수님 보안 수업 + AI 검증","prof","OWASP Top 10 대응 + Java 21 업그레이드",
            "[{\"label\":\"Java 17 -> 21 업그레이드\",\"before\":\"Java 17 toolchain. gradle-wrapper.jar 누락으로 clone 후 빌드 불가\",\"after\":\"Java 21 toolchain + foojay-resolver(JDK 자동 조달) + wrapper jar 커밋\",\"reason\":\"서버 배포 환경 통일 및 최신 LTS 적용\"},"
            +"{\"label\":\"@Builder.Default 누락 버그 수정\",\"before\":\"@Builder 사용 시 isDeleted/useYn/status 등 기본값이 무시되고 null로 저장될 위험\",\"after\":\"16개 엔티티/DTO의 기본값 필드에 @Builder.Default 추가\",\"reason\":\"빌더 생성 시 NOT NULL 위반 및 NPE 방지\"},"
            +"{\"label\":\"OWASP A05 보안 설정 노출 차단\",\"before\":\"show-sql=true, format_sql=true, 로그 DEBUG로 운영 시 SQL/테이블/컬럼 구조 노출\",\"after\":\"운영 기본값 off로 변경. 환경변수(JPA_SHOW_SQL 등)로만 토글\",\"reason\":\"교수님: '스택 트레이스/쿼리 노출로 내부 구조가 유출된다'\"},"
            +"{\"label\":\"OWASP A02 시크릿 외부화\",\"before\":\"DB 비밀번호/JWT 시크릿이 application.properties에 평문 하드코딩\",\"after\":\"${DB_PASSWORD}, ${JWT_SECRET} 환경변수로 분리(기본값 fallback 유지)\",\"reason\":\"교수님: '민감 정보는 단방향/외부화로 보호해야 한다'\"},"
            +"{\"label\":\"OWASP A07 로그인 무차별 대입 방어\",\"before\":\"로그인 실패 횟수 제한 없음 -> 무한 시도 가능\",\"after\":\"LoginAttemptService 추가. 5회 실패 시 5분 일시 잠금\",\"reason\":\"교수님: '인증 시도에 제한을 두고 카운팅/잠금이 필요하다'\"}]");
    }

    private void seed(int round, String date, String source, String type, String title, String changes) {
        // 이미 존재하는 라운드는 건너뛴다(기존 DB와 충돌 방지, 신규 라운드만 추가).
        if (designHistoryRepository.existsByRoundAndIsDeletedFalse(round)) return;
        designHistoryRepository.save(DesignHistory.builder()
                .round(round).roundDate(date).source(source).sourceType(type).title(title).changes(changes).build());
        log.info("[INIT] 설계 이력 라운드 {} 시드 완료", round);
    }
}
