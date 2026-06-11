package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 생체정보 응답. 기존 Map 키 유지(서비스에서 빌드 — 응급여부 계산 필요). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BiometricResponse {
    private Long id;
    private String branchName;
    private Long deviceId;
    private String modelName;
    private String battery;
    private String userName;
    private LocalDate latestUseDate;
    private String latestUseTime;
    private String useTimePerDay;
    private Integer breathPerDay;
    private Integer stepsPerDay;
    private String totalUseTime;
    private Integer totalSteps;
    private String emergencyYn;
    private LocalDateTime emergencyRecordTime;
    private LocalDateTime latestUpdateTime;
    private String latestLocation;
}
