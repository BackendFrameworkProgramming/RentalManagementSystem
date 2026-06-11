package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;
import java.util.List;

/** 디바이스 다중 선택 요청(지점 해제 등). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchDeviceIdsRequest {
    private List<Long> deviceIds;
}
