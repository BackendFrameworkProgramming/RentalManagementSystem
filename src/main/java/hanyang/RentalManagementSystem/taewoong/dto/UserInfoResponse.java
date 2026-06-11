package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 현재 사용자 정보(/api/auth/me) 응답. 기존 Map 키와 동일. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserInfoResponse {
    private Long id;
    private String userName;
    private String userLoginId;
    private String role;
    private String email;
    private String contact;
}
