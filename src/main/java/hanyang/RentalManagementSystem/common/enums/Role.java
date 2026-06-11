package hanyang.RentalManagementSystem.common.enums;

/**
 * 사용자 역할(권한 계층). ADMIN &gt; BRANCH_MANAGER &gt; STAFF &gt; USER
 * <ul>
 *   <li>ADMIN          : 시스템/전체 관리자 (모든 메뉴/데이터)</li>
 *   <li>BRANCH_MANAGER : 지점 관리자 (본인 지점 범위 데이터)</li>
 *   <li>STAFF          : 센터 운영 담당자 — 비관리자 로그인의 기본값. 운영화면 8개 전체 조회</li>
 *   <li>USER           : (확장용) 디바이스 착용자/신청자가 직접 로그인하는 경우. 본인 데이터만.
 *                        현행 운영 범위에서는 착용자를 Rental.user '데이터'로만 관리하고 로그인 주체로 쓰지 않음.</li>
 *   <li>BRANCH_MANAGER : (확장용) 지점 단위 관리자. 본인 지점만.</li>
 * </ul>
 * 현행 운영 역할은 ADMIN(관리자) / STAFF(운영자) 2단계. USER·BRANCH_MANAGER는 확장 여지로 enum에 보존.
 * OWASP A01(접근 통제) 및 교수님 피드백(상태/역할은 문자열 하드코딩 대신 Enum) 반영.
 */
public enum Role {
    ADMIN,
    BRANCH_MANAGER,
    STAFF,
    USER;

    /** DB에 null/알 수 없는 값이면 운영 기본값 STAFF로 수렴(기존 데이터에 role=null 존재). */
    public static Role fromString(String value) {
        if (value == null || value.isBlank()) return STAFF;
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STAFF;
        }
    }
}
