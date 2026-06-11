package hanyang.RentalManagementSystem.gyumin.dto;

import lombok.*;

/** 지점별 임대 요약. 기존 Map 키(branchId, branchName, totalCount, rentingCount) 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RentalBranchSummaryResponse {
    private Long branchId;
    private String branchName;
    private long totalCount;
    private long rentingCount;
}
