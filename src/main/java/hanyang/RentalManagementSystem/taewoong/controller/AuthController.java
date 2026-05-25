package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.taewoong.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Map<String, Object>>> signup(
            @RequestBody Map<String, String> body, HttpServletResponse response) {
        Map<String, Object> tokens = authService.signup(body);
        setCookies(response, tokens);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.created(tokens));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<Map<String, Object>>> login(
            @RequestBody Map<String, String> body, HttpServletResponse response) {
        Map<String, Object> tokens = authService.login(body);
        setCookies(response, tokens);
        return ResponseEntity.ok(CommonResponse.success(tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<Map<String, Object>>> refresh(
            HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refresh_token");
        Map<String, Object> tokens = authService.refresh(refreshToken);
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
    public ResponseEntity<CommonResponse<Map<String, Object>>> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(CommonResponse.success(authService.getCurrentUser(userId)));
    }

    private void setCookies(HttpServletResponse response, Map<String, Object> tokens) {
        Cookie access = new Cookie("access_token", (String) tokens.get("accessToken"));
        access.setHttpOnly(true);
        access.setPath("/");
        access.setMaxAge(86400);
        response.addCookie(access);

        Cookie refresh = new Cookie("refresh_token", (String) tokens.get("refreshToken"));
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
