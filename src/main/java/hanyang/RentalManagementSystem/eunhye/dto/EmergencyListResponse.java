package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;
import java.util.List;

/** 응급기록 목록 응답. 기존 Map 키 {emergencyRecords:[...]} 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyListResponse {
    private List<EmergencyResponse> emergencyRecords;
}
