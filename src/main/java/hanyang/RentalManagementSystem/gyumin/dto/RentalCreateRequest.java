package hanyang.RentalManagementSystem.gyumin.dto;

import lombok.*;

/** 임대 신청 요청 (교수님 피드백 #4: Map 대신 DTO). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RentalCreateRequest {
    private Long deviceId;
    private Long userId;
    private Long branchId;
}
