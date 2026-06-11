package hanyang.RentalManagementSystem.taewoong.dto;

import hanyang.RentalManagementSystem.common.entity.User;
import lombok.*;

/** 사용자 목록/단건 응답. 기존 Map 키(id,userName,userLoginId,role,contact,email)와 동일하게 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String userName;
    private String userLoginId;
    private String role;
    private String contact;
    private String email;
    private Long employeeId;
    private Long branchId;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .userName(u.getUserName())
                .userLoginId(u.getUserLoginId())
                .role(u.getRole() == null ? null : u.getRole().name())
                .contact(u.getContact())
                .email(u.getEmail())
                .employeeId(u.getEmployee() != null ? u.getEmployee().getId() : null)
                .branchId(u.getBranch() != null ? u.getBranch().getId() : null)
                .build();
    }
}
