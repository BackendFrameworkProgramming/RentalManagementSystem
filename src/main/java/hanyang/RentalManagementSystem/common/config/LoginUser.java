package hanyang.RentalManagementSystem.common.config;

import hanyang.RentalManagementSystem.common.enums.Role;

/**
 * JWT에서 추출한 인증 주체. SecurityContext의 principal로 보관된다.
 * userId/role 외에 branchId를 담아 지점 단위 데이터 스코핑(IDOR 방어)에 사용.
 */
public record LoginUser(Long userId, Role role, Long branchId) {
}
