package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 디바이스-지점 연결(단건) 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchLinkRequest {
    private Long branchId;
}
