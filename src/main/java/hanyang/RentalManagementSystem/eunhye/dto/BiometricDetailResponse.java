package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;
import java.util.List;

/** 생체정보 상세 응답. 기존 Map 키 {biometricData, emergencyRecords} 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BiometricDetailResponse {
    private BiometricResponse biometricData;
    private List<EmergencyResponse> emergencyRecords;
}
