package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 로그인 요청 (교수님 피드백 #4: Map 대신 DTO). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequest {
    private String userLoginId;
    private String password;
}
