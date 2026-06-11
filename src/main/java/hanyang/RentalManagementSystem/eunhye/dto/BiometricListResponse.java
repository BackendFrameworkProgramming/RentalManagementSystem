package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;
import java.util.List;

/** 생체정보 목록 응답. 기존 Map 키 {biometricData:[...]} 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BiometricListResponse {
    private List<BiometricResponse> biometricData;
}
