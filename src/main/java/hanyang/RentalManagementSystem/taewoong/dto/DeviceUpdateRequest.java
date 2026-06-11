package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 디바이스 수정 요청. null 필드는 변경하지 않음. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceUpdateRequest {
    private String status;
    private String battery;
    private String remark;
    private Long branchId;
}
