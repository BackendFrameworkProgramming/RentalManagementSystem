package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DesignHistoryUpsertRequest {
    private Integer round;
    private String roundDate;
    private String source;
    private String sourceType;
    private String title;
    private String changes; // JSON 문자열
}
