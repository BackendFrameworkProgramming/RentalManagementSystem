package hanyang.RentalManagementSystem.minseok.dto;

import lombok.*;

/** 지점 담당자 등록/수정 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchManagerUpsertRequest {
    private Long branchId;
    private String managerName;
    private String contact;
    private String email;
    private String managerType; // 주 / 부
    private Boolean status;
}
