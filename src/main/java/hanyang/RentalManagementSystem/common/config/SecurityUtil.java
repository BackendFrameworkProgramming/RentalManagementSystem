package hanyang.RentalManagementSystem.common.config;

import hanyang.RentalManagementSystem.common.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 현재 로그인 사용자(역할/지점) 조회 유틸.
 * 서비스 계층에서 데이터 스코핑(본인/본인 지점만 조회) 및 권한 검증(IDOR 방어)에 사용한다.
 * OWASP A01(접근 통제) — 교수님 강조: 권한 검증은 메소드/서비스 단위로.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static LoginUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    public static Long currentUserId() {
        LoginUser u = current();
        return u == null ? null : u.userId();
    }

    public static Role currentRole() {
        LoginUser u = current();
        return u == null ? null : u.role();
    }

    public static Long currentBranchId() {
        LoginUser u = current();
        return u == null ? null : u.branchId();
    }

    public static boolean isAdmin() {
        return currentRole() == Role.ADMIN;
    }

    public static boolean isBranchManager() {
        return currentRole() == Role.BRANCH_MANAGER;
    }

    public static boolean isStaff() {
        return currentRole() == Role.STAFF;
    }

    public static boolean isUser() {
        return currentRole() == Role.USER;
    }

    /** ADMIN/STAFF는 전체 데이터 조회 가능. BRANCH_MANAGER/USER는 범위 제한. */
    public static boolean canSeeAll() {
        Role r = currentRole();
        return r == Role.ADMIN || r == Role.STAFF;
    }
}
