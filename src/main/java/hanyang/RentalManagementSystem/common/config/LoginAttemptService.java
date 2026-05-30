package hanyang.RentalManagementSystem.common.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * OWASP A07(인증 실패) 대응: 로그인 무차별 대입(brute-force) 방어.
 * 로그인 아이디별 실패 횟수를 집계하고, 임계치를 넘으면 일정 시간 잠근다.
 * 단순 인메모리 구현이라 서버 재시작 시 초기화된다(과제/단일 인스턴스 기준 충분).
 */
@Component
public class LoginAttemptService {

    /** 잠금까지 허용하는 최대 실패 횟수 */
    private static final int MAX_ATTEMPTS = 5;
    /** 임계치 초과 시 잠금 유지 시간(밀리초) - 5분 */
    private static final long LOCK_TIME_MS = 5 * 60 * 1000L;

    private static class Attempt {
        int count;
        long lockedUntil;
    }

    private final ConcurrentHashMap<String, Attempt> cache = new ConcurrentHashMap<>();

    /** 현재 잠겨 있는지 여부 */
    public boolean isLocked(String loginId) {
        Attempt a = cache.get(loginId);
        if (a == null) return false;
        if (a.lockedUntil > System.currentTimeMillis()) {
            return true;
        }
        // 잠금 시간이 지났으면 해제
        if (a.lockedUntil != 0) {
            cache.remove(loginId);
        }
        return false;
    }

    /** 로그인 실패 기록. 임계치 초과 시 잠금 설정 */
    public void loginFailed(String loginId) {
        Attempt a = cache.computeIfAbsent(loginId, k -> new Attempt());
        synchronized (a) {
            a.count++;
            if (a.count >= MAX_ATTEMPTS) {
                a.lockedUntil = System.currentTimeMillis() + LOCK_TIME_MS;
            }
        }
    }

    /** 로그인 성공 시 기록 초기화 */
    public void loginSucceeded(String loginId) {
        cache.remove(loginId);
    }

    /** 남은 잠금 시간(초). 잠겨있지 않으면 0 */
    public long remainingLockSeconds(String loginId) {
        Attempt a = cache.get(loginId);
        if (a == null) return 0;
        long remain = a.lockedUntil - System.currentTimeMillis();
        return remain > 0 ? remain / 1000 : 0;
    }
}
