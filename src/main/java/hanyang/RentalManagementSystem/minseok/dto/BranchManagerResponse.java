package hanyang.RentalManagementSystem.minseok.dto;

import hanyang.RentalManagementSystem.common.entity.Branch;
import hanyang.RentalManagementSystem.common.entity.BranchManager;
import lombok.*;

/** 지점 담당자 응답. 기존 Map 키 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchManagerResponse {
    private Long id;
    private Long branchId;
    private String branchName;
    private String managerName;
    private String contact;
    private String email;
    private String managerType;
    private Boolean status;

    public static BranchManagerResponse from(BranchManager m, Branch branch) {
        return BranchManagerResponse.builder()
                .id(m.getId())
                .branchId(branch.getId())
                .branchName(branch.getBranchName())
                .managerName(m.getManagerName())
                .contact(m.getContact())
                .email(m.getEmail())
                .managerType(m.getManagerType())
                .status(m.getStatus())
                .build();
    }
}
