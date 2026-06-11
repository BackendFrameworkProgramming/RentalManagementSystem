package hanyang.RentalManagementSystem.gyumin.dto;

import lombok.*;

/** 지점별 A/S 요약. 기존 Map 키(branchId, branchName, totalCount, processingCount) 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsBranchSummaryResponse {
    private Long branchId;
    private String branchName;
    private long totalCount;
    private long processingCount;
}
