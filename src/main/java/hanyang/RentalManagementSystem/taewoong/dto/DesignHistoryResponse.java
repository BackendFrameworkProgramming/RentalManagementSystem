package hanyang.RentalManagementSystem.taewoong.dto;

import hanyang.RentalManagementSystem.common.entity.DesignHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DesignHistoryResponse {
    private Long id;
    private Integer round;
    private String roundDate;
    private String source;
    private String sourceType;
    private String title;
    private String changes;
    private LocalDateTime createdAt;

    public static DesignHistoryResponse from(DesignHistory h) {
        return DesignHistoryResponse.builder()
                .id(h.getId())
                .round(h.getRound())
                .roundDate(h.getRoundDate())
                .source(h.getSource())
                .sourceType(h.getSourceType())
                .title(h.getTitle())
                .changes(h.getChanges())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
