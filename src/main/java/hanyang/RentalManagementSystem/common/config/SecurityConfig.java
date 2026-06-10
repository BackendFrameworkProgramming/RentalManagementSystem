package hanyang.RentalManagementSystem.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // OWASP A05(보안 설정 오류): 보안 HTTP 응답 헤더.
    // 모든 리소스가 same-origin(템플릿 인라인 스크립트/스타일 포함)이므로 'self' 기반 정책.
    // 인라인 스크립트/onclick/style이 다수 존재해 script-src/style-src는 'unsafe-inline' 허용(실용형).
    private static final String CONTENT_SECURITY_POLICY =
            "default-src 'self'; "
            + "script-src 'self' 'unsafe-inline'; "
            + "style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data:; "
            + "font-src 'self'; "
            + "connect-src 'self'; "
            + "form-action 'self'; "
            + "frame-ancestors 'none'; "
            + "base-uri 'self'; "
            + "object-src 'none'";

    // 사용하지 않는 강력 기능을 전면 차단.
    private static final String PERMISSIONS_POLICY =
            "geolocation=(), microphone=(), camera=(), payment=(), usb=(), "
            + "magnetometer=(), gyroscope=(), accelerometer=()";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // OWASP A05: 보안 응답 헤더(HSTS/CSP/Referrer-Policy/Permissions-Policy/X-Frame-Options/X-Content-Type-Options)
            .headers(headers -> headers
                // HSTS: TLS는 앞단 프록시가 종료하고 앱은 평문 HTTP로 수신하므로
                // 기본(secure 요청 한정) 대신 모든 요청에서 방출하도록 AnyRequestMatcher 지정.
                .httpStrictTransportSecurity(hsts -> hsts
                    .requestMatcher(AnyRequestMatcher.INSTANCE)
                    .includeSubDomains(false)
                    .preload(false)
                    .maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY))
                .referrerPolicy(ref -> ref.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicyHeader(pp -> pp.policy(PERMISSIONS_POLICY))
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(opts -> {})
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/api/auth/login", "/api/auth/refresh").permitAll()
                .requestMatchers("/css/**", "/js/**", "/favicon.ico").permitAll()
                // OWASP A01(접근 통제): 회원 가입(계정 생성)은 관리자만 가능하도록 제한
                .requestMatchers("/api/auth/signup").hasRole("ADMIN")
                .requestMatchers("/system-logs", "/models", "/common-codes", "/users", "/design-history").hasRole("ADMIN")
                .requestMatchers("/api/system-logs/**", "/api/models/**", "/api/common-codes/**", "/api/users/**", "/api/design-history/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                            "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}}");
                    } else {
                        response.sendRedirect("/login");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                            "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"관리자 권한이 필요합니다.\"}}");
                    } else {
                        response.sendRedirect("/devices");
                    }
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
