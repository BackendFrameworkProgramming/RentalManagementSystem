package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "department")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "dept_name", length = 50, nullable = false)
    private String deptName;
    @Column(name = "created_date")
    private LocalDate createdDate;
    @Column(name = "applied_date")
    private LocalDate appliedDate;
    @Column(name = "use_yn", nullable = false)
    @Builder.Default
    private Boolean useYn = true;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    @Column(name = "sort_order")
    private Integer sortOrder;
}
