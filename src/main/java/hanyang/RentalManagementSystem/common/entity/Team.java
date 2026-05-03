package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "team")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Team extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    @Column(name = "team_name", length = 50, nullable = false)
    private String teamName;
    @Column(name = "created_date")
    private LocalDate createdDate;
    @Column(name = "applied_date")
    private LocalDate appliedDate;
    @Column(name = "use_yn", nullable = false)
    private Boolean useYn = true;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    @Column(name = "sort_order")
    private Integer sortOrder;
}
