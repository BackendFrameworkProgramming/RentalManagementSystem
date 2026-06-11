package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;
import java.util.List;

/** 지점별/전체 디바이스 수량 집계 응답. 기존 Map 키 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceSummaryResponse {
    private List<BranchCount> branches;
    private long total;
    private long unshipped;
    private long shipped;
    private long returned;
    private long disposed;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BranchCount {
        private Long branchId;
        private String branchName;
        private long count;
    }
}
