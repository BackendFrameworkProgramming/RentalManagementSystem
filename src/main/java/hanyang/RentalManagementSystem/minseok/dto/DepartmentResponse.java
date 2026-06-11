package hanyang.RentalManagementSystem.minseok.dto;

import hanyang.RentalManagementSystem.common.entity.Department;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentResponse {
    private Long id;
    private String deptName;
    private LocalDate createdDate;
    private LocalDate appliedDate;
    private Boolean useYn;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepartmentResponse from(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .deptName(d.getDeptName())
                .createdDate(d.getCreatedDate())
                .appliedDate(d.getAppliedDate())
                .useYn(d.getUseYn())
                .sortOrder(d.getSortOrder())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
