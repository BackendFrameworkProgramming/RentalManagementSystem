package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.config.LoginUser;
import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.taewoong.dto.AuthTokenResponse;
import hanyang.RentalManagementSystem.taewoong.dto.LoginRequest;
import hanyang.RentalManagementSystem.taewoong.dto.SignupRequest;
import hanyang.RentalManagementSystem.taewoong.dto.UserInfoResponse;
import hanyang.RentalManagementSystem.taewoong.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<AuthTokenResponse>> signup(
            @RequestBody SignupRequest req, HttpServletResponse response) {
        AuthTokenResponse tokens = authService.signup(req);
        setCookies(response, tokens);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.created(tokens));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<AuthTokenResponse>> login(
            @RequestBody LoginRequest req, HttpServletResponse response) {
        AuthTokenResponse tokens = authService.login(req);
        setCookies(response, tokens);
        return ResponseEntity.ok(CommonResponse.success(tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<AuthTokenResponse>> refresh(
            HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refresh_token");
        AuthTokenResponse tokens = authService.refresh(refreshToken);
        setCookies(response, tokens);
        return ResponseEntity.ok(CommonResponse.success(tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
            HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refresh_token");
        authService.logout(refreshToken);
        clearCookies(response);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserInfoResponse>> me(Authentication authentication) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        return ResponseEntity.ok(CommonResponse.success(authService.getCurrentUser(userId)));
    }

    private void setCookies(HttpServletResponse response, AuthTokenResponse tokens) {
        addAuthCookie(response, "access_token", tokens.getAccessToken(), "/", 86400);
        addAuthCookie(response, "refresh_token", tokens.getRefreshToken(), "/api/auth", 604800);
    }

    private void clearCookies(HttpServletResponse response) {
        addAuthCookie(response, "access_token", "", "/", 0);
        addAuthCookie(response, "refresh_token", "", "/api/auth", 0);
    }

    // 보안 쿠키: HttpOnly(JS 접근 차단) + Secure(HTTPS 전용 전송) + SameSite=Strict(CSRF 방어)
    private void addAuthCookie(HttpServletResponse response, String name, String value, String path, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (name.equals(c.getName())) return c.getValue();
            }
        }
        return null;
    }
}
