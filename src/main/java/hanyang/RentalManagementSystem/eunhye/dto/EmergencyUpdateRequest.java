package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;

/** 응급기록 수정 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyUpdateRequest {
    private String actionContent;
    private String actionResult;
}
