package hanyang.RentalManagementSystem.minseok.dto;

import hanyang.RentalManagementSystem.common.entity.Team;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamResponse {
    private Long id;
    private String teamName;
    private Long departmentId;
    private String departmentName;
    private LocalDate createdDate;
    private LocalDate appliedDate;
    private Boolean useYn;
    private Integer sortOrder;

    public static TeamResponse from(Team t) {
        return TeamResponse.builder()
                .id(t.getId())
                .teamName(t.getTeamName())
                .departmentId(t.getDepartment() != null ? t.getDepartment().getId() : null)
                .departmentName(t.getDepartment() != null ? t.getDepartment().getDeptName() : null)
                .createdDate(t.getCreatedDate())
                .appliedDate(t.getAppliedDate())
                .useYn(t.getUseYn())
                .sortOrder(t.getSortOrder())
                .build();
    }
}
