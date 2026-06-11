package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;

/** 생체정보 등록 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BiometricCreateRequest {
    private Long deviceId;
    private String userName;
    private String latestUseDate; // ISO yyyy-MM-dd
    private String latestUseTime;
    private String useTimePerDay;
    private Integer breathPerDay;
    private Integer stepsPerDay;
    private String totalUseTime;
    private Integer totalSteps;
    private String latestLocation;
}
