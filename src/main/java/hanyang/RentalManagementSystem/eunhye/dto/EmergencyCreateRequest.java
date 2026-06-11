package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;

/** 응급기록 등록 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyCreateRequest {
    private Long biometricDataId;
    private String emergencyType;
    private String actionContent;
    private String actionResult;
}
