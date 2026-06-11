package hanyang.RentalManagementSystem.gyumin.dto;

import lombok.*;

/** A/S 등록 요청 (교수님 피드백 #4: Map 대신 DTO). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsRecordCreateRequest {
    private Long deviceId;
    private Long rentalId;
    private Long branchId;
    private String asDescription;
}
