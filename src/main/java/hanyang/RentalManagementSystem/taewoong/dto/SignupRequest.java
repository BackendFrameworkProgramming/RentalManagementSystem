package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 회원가입 요청 (ADMIN 전용). 교수님 피드백 #4: Map 대신 DTO. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SignupRequest {
    private String userLoginId;
    private String password;
    private String userName;
    private String contact;
    private String email;
}
