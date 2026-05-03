package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "employee")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    @Column(name = "emp_name", length = 50, nullable = false)
    private String empName;
    @Column(name = "emp_no", length = 20)
    private String empNo;
    @Column(name = "job_title", length = 30)
    private String jobTitle;
    @Column(name = "employment_type", length = 20, nullable = false)
    private String employmentType;
    @Column(name = "work_status", length = 20, nullable = false)
    private String workStatus;
    @Column(name = "work_status_date")
    private LocalDate workStatusDate;
    @Column(name = "hire_date")
    private LocalDate hireDate;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    @Column(columnDefinition = "TEXT")
    private String remark;
}
