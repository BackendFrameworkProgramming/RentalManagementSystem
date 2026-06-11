package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 디바이스 등록 요청 (교수님 피드백 #4: Map 대신 DTO). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceCreateRequest {
    private String deviceId;
    private Long modelVersionId;
    private String battery;
    private String remark;
    private Long branchId;
}
