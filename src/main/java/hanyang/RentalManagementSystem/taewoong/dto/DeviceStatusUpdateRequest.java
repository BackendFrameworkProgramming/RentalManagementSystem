package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 디바이스 상태 변경 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceStatusUpdateRequest {
    private String status;
}
