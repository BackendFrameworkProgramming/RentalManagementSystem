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
import org.springframework.http.HttpStatus;
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
        Cookie access = new Cookie("access_token", tokens.getAccessToken());
        access.setHttpOnly(true);
        access.setPath("/");
        access.setMaxAge(86400);
        response.addCookie(access);

        Cookie refresh = new Cookie("refresh_token", tokens.getRefreshToken());
        refresh.setHttpOnly(true);
        refresh.setPath("/api/auth");
        refresh.setMaxAge(604800);
        response.addCookie(refresh);
    }

    private void clearCookies(HttpServletResponse response) {
        Cookie access = new Cookie("access_token", "");
        access.setHttpOnly(true);
        access.setPath("/");
        access.setMaxAge(0);
        response.addCookie(access);

        Cookie refresh = new Cookie("refresh_token", "");
        refresh.setHttpOnly(true);
        refresh.setPath("/api/auth");
        refresh.setMaxAge(0);
        response.addCookie(refresh);
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
