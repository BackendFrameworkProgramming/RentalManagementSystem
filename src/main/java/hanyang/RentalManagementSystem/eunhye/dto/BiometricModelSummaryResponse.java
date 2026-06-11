package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;
import java.util.Map;

/** 모델별 생체정보 건수 요약. 기존 Map 키 {summary:{...}} 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BiometricModelSummaryResponse {
    private Map<String, Long> summary;
}
