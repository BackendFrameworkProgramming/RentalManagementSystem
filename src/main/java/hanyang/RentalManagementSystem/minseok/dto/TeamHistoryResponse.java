package hanyang.RentalManagementSystem.minseok.dto;

import hanyang.RentalManagementSystem.common.entity.TeamHistory;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamHistoryResponse {
    private Long id;
    private Long teamId;
    private String changeType;
    private String beforeValue;
    private String afterValue;
    private LocalDate changedDate;
    private String changedBy;
    private LocalDateTime createdAt;

    public static TeamHistoryResponse from(TeamHistory h) {
        return TeamHistoryResponse.builder()
                .id(h.getId())
                .teamId(h.getTeam() != null ? h.getTeam().getId() : null)
                .changeType(h.getChangeType())
                .beforeValue(h.getBeforeValue())
                .afterValue(h.getAfterValue())
                .changedDate(h.getChangedDate())
                .changedBy(h.getChangedBy())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
