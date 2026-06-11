package hanyang.RentalManagementSystem.minseok.dto;

import hanyang.RentalManagementSystem.common.entity.DeptHistory;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeptHistoryResponse {
    private Long id;
    private Long departmentId;
    private String changeType;
    private String beforeValue;
    private String afterValue;
    private LocalDate changedDate;
    private String changedBy;
    private LocalDateTime createdAt;

    public static DeptHistoryResponse from(DeptHistory h) {
        return DeptHistoryResponse.builder()
                .id(h.getId())
                .departmentId(h.getDepartment() != null ? h.getDepartment().getId() : null)
                .changeType(h.getChangeType())
                .beforeValue(h.getBeforeValue())
                .afterValue(h.getAfterValue())
                .changedDate(h.getChangedDate())
                .changedBy(h.getChangedBy())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
