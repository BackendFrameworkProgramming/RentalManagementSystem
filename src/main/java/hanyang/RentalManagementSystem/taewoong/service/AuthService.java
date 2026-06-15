package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.config.JwtTokenProvider;
import hanyang.RentalManagementSystem.common.config.LoginAttemptService;
import hanyang.RentalManagementSystem.common.entity.RefreshToken;
import hanyang.RentalManagementSystem.common.entity.User;
import hanyang.RentalManagementSystem.common.enums.Role;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.RefreshTokenRepository;
import hanyang.RentalManagementSystem.common.repository.UserRepository;
import hanyang.RentalManagementSystem.taewoong.dto.AuthTokenResponse;
import hanyang.RentalManagementSystem.taewoong.dto.LoginRequest;
import hanyang.RentalManagementSystem.taewoong.dto.SignupRequest;
import hanyang.RentalManagementSystem.taewoong.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public AuthTokenResponse signup(SignupRequest req) {
        if (isBlank(req.getUserLoginId()) || isBlank(req.getPassword()) || isBlank(req.getUserName())) {
            throw new CustomException("INVALID_REQUEST", "아이디, 비밀번호, 이름은 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByUserLoginIdAndIsDeletedFalse(req.getUserLoginId())) {
            throw new CustomException("DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .userLoginId(req.getUserLoginId())
                .password(passwordEncoder.encode(req.getPassword()))
                .userName(req.getUserName())
                .contact(req.getContact() != null ? req.getContact() : "")
                .email(req.getEmail() != null ? req.getEmail() : "")
                .role(Role.STAFF)
                .build();
        // existsBy 검사와 save 사이의 race condition(교수님 ③) 방어:
        // 유니크 제약 위반은 500이 아니라 409로 정상 처리
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException("DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT);
        }

        return generateTokens(user);
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest req) {
        String loginId = req.getUserLoginId();

        // OWASP A07(인증 실패): 무차별 대입 방어 - 임계치 초과 시 일시 잠금
        if (loginId != null && loginAttemptService.isLocked(loginId)) {
            long remain = loginAttemptService.remainingLockSeconds(loginId);
            throw new CustomException("ACCOUNT_LOCKED",
                    "로그인 시도 횟수를 초과했습니다. 약 " + ((remain / 60) + 1) + "분 후 다시 시도해주세요.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        User user = userRepository.findByUserLoginIdAndIsDeletedFalse(loginId).orElse(null);

        if (user == null || user.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            if (loginId != null) loginAttemptService.loginFailed(loginId);
            throw new CustomException("LOGIN_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        loginAttemptService.loginSucceeded(loginId);
        refreshTokenRepository.deleteByUserId(user.getId());
        return generateTokens(user);
    }

    @Transactional
    public AuthTokenResponse refresh(String refreshTokenStr) {
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

    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return UserInfoResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .userLoginId(user.getUserLoginId())
                .role((user.getRole() == null ? Role.STAFF : user.getRole()).name())
                .email(user.getEmail())
                .contact(user.getContact())
                .build();
    }

    private AuthTokenResponse generateTokens(User user) {
        Role role = user.getRole() == null ? Role.STAFF : user.getRole();
        // 지점 관리자라면 branchId를 토큰에 담아 데이터 스코핑에 활용
        Long branchId = user.getBranch() != null ? user.getBranch().getId() : null;

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUserLoginId(), role.name(), branchId);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        RefreshToken entity = RefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000))
                .build();
        refreshTokenRepository.save(entity);

        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userName(user.getUserName())
                .role(role.name())
                .build();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
