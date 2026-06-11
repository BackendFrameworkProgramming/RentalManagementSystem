package hanyang.RentalManagementSystem.common.enums;

/**
 * 사용자 역할(권한 계층). ADMIN &gt; BRANCH_MANAGER &gt; STAFF &gt; USER
 * <ul>
 *   <li>ADMIN          : 시스템/전체 관리자 (모든 메뉴/데이터)</li>
 *   <li>BRANCH_MANAGER : 지점 관리자 (본인 지점 범위 데이터)</li>
 *   <li>STAFF          : 센터/업체 직원(담당자)</li>
 *   <li>USER           : 일반 사용자 = 디바이스 착용자/임대 신청자 (본인 데이터만)</li>
 * </ul>
 * OWASP A01(접근 통제) 및 교수님 피드백(상태/역할은 문자열 하드코딩 대신 Enum) 반영.
 */
public enum Role {
    ADMIN,
    BRANCH_MANAGER,
    STAFF,
    USER;

    /** DB에 null 또는 알 수 없는 값이 있어도 안전하게 USER로 수렴(기존 데이터에 role=null 존재). */
    public static Role fromString(String value) {
        if (value == null || value.isBlank()) return USER;
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER;
        }
    }
}
