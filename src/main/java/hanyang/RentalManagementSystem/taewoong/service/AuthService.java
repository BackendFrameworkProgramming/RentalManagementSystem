package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.config.JwtTokenProvider;
import hanyang.RentalManagementSystem.common.config.LoginAttemptService;
import hanyang.RentalManagementSystem.common.entity.RefreshToken;
import hanyang.RentalManagementSystem.common.entity.User;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.RefreshTokenRepository;
import hanyang.RentalManagementSystem.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public Map<String, Object> signup(Map<String, String> body) {
        String loginId = body.get("userLoginId");
        String password = body.get("password");
        String userName = body.get("userName");

        if (loginId == null || loginId.isBlank() || password == null || password.isBlank() || userName == null || userName.isBlank()) {
            throw new CustomException("INVALID_REQUEST", "아이디, 비밀번호, 이름은 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByUserLoginIdAndIsDeletedFalse(loginId)) {
            throw new CustomException("DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .userLoginId(loginId)
                .password(passwordEncoder.encode(password))
                .userName(userName)
                .contact(body.getOrDefault("contact", ""))
                .email(body.getOrDefault("email", ""))
                .role("USER")
                .build();
        userRepository.save(user);

        return generateTokens(user);
    }

    @Transactional
    public Map<String, Object> login(Map<String, String> body) {
        String loginId = body.get("userLoginId");
        String password = body.get("password");

        // OWASP A07(인증 실패): 무차별 대입 방어 - 임계치 초과 시 일시 잠금
        if (loginId != null && loginAttemptService.isLocked(loginId)) {
            long remain = loginAttemptService.remainingLockSeconds(loginId);
            throw new CustomException("ACCOUNT_LOCKED",
                    "로그인 시도 횟수를 초과했습니다. 약 " + ((remain / 60) + 1) + "분 후 다시 시도해주세요.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        User user = userRepository.findByUserLoginIdAndIsDeletedFalse(loginId).orElse(null);

        if (user == null || user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            if (loginId != null) loginAttemptService.loginFailed(loginId);
            throw new CustomException("LOGIN_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        loginAttemptService.loginSucceeded(loginId);
        refreshTokenRepository.deleteByUserId(user.getId());
        return generateTokens(user);
    }

    @Transactional
    public Map<String, Object> refresh(String refreshTokenStr) {
        if (refreshTokenStr == null || !jwtTokenProvider.validateToken(refreshTokenStr)) {
            throw new CustomException("INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED);
        }

        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new CustomException("INVALID_REFRESH_TOKEN", "리프레시 토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new CustomException("EXPIRED_REFRESH_TOKEN", "리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByIdAndIsDeletedFalse(stored.getUserId())
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        refreshTokenRepository.delete(stored);
        return generateTokens(user);
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null) {
            refreshTokenRepository.deleteByToken(refreshTokenStr);
        }
    }

    public Map<String, Object> getCurrentUser(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("userName", user.getUserName());
        map.put("userLoginId", user.getUserLoginId());
        map.put("role", user.getRole());
        map.put("email", user.getEmail());
        map.put("contact", user.getContact());
        return map;
    }

    private Map<String, Object> generateTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUserLoginId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        RefreshToken entity = RefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000))
                .build();
        refreshTokenRepository.save(entity);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userName", user.getUserName());
        result.put("role", user.getRole());
        return result;
    }
}
