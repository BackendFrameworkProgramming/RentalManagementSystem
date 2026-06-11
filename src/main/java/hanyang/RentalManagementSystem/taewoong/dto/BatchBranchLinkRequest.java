package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;
import java.util.List;

/** 디바이스-지점 연결(다중) 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchBranchLinkRequest {
    private List<Long> deviceIds;
    private Long branchId;
}
