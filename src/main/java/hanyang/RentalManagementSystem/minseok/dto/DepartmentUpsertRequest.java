package hanyang.RentalManagementSystem.minseok.dto;

import lombok.*;

/** 부서 등록/수정 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentUpsertRequest {
    private String deptName;
    private Boolean useYn;
    private String appliedDate; // ISO yyyy-MM-dd
    private Integer sortOrder;
    private String changedBy;
}
