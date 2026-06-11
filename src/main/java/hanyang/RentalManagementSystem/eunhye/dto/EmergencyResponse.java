package hanyang.RentalManagementSystem.eunhye.dto;

import hanyang.RentalManagementSystem.common.entity.EmergencyRecord;
import lombok.*;

import java.time.LocalDateTime;

/** 응급기록 응답. 기존 Map 키 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyResponse {
    private Long id;
    private Long biometricDataId;
    private String emergencyType;
    private LocalDateTime emergencyRecordTime;
    private String actionContent;
    private String actionResult;

    public static EmergencyResponse from(EmergencyRecord e) {
        return EmergencyResponse.builder()
                .id(e.getId())
                .biometricDataId(e.getBiometricData() != null ? e.getBiometricData().getId() : null)
                .emergencyType(e.getEmergencyType())
                .emergencyRecordTime(e.getEmergencyRecordTime())
                .actionContent(e.getActionContent())
                .actionResult(e.getActionResult())
                .build();
    }
}
