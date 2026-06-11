package hanyang.RentalManagementSystem.minseok.dto;

import lombok.*;

/** 지점 등록/수정 요청. null 필드는 변경하지 않음. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchUpsertRequest {
    private String branchName;
    private Boolean status;
    private String address;
    private String addressDetail;
    private String managerName;
    private String phone;
    private String fax;
    private String appliedDate; // ISO yyyy-MM-dd
}
