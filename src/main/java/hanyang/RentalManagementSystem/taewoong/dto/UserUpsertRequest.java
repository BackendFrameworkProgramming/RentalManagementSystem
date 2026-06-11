package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/**
 * 사용자 생성/수정 요청 (교수님 피드백 #4: Map 대신 DTO).
 * role/employeeId/branchId로 역할 및 지점/직원 연결을 지정한다(역할 모델).
 * 수정 시 null 필드는 "변경 안 함"으로 간주.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserUpsertRequest {
    private String userLoginId;
    private String password;
    private String role;       // ADMIN / BRANCH_MANAGER / STAFF / USER
    private String userName;
    private String contact;
    private String email;
    private Long employeeId;   // STAFF 연결용 (선택)
    private Long branchId;     // BRANCH_MANAGER 연결용 (선택)
}
