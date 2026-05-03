package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "dept_history")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeptHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    @Column(name = "change_type", length = 20, nullable = false)
    private String changeType;
    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;
    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;
    @Column(name = "changed_date")
    private LocalDate changedDate;
    @Column(name = "changed_by", length = 50)
    private String changedBy;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
