package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 로그인/회원가입/토큰갱신 응답. JSON 필드명은 기존 Map 계약과 동일하게 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String userName;
    private String role;
}
