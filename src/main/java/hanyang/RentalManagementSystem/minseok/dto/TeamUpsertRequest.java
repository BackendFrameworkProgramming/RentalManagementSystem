package hanyang.RentalManagementSystem.minseok.dto;

import lombok.*;

/** 팀 등록/수정 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamUpsertRequest {
    private Long departmentId;
    private String teamName;
    private Boolean useYn;
    private String appliedDate; // ISO yyyy-MM-dd
    private Integer sortOrder;
    private String changedBy;
}
