package hanyang.RentalManagementSystem.common.entity;

import hanyang.RentalManagementSystem.common.enums.EmploymentType;
import hanyang.RentalManagementSystem.common.enums.WorkStatus;
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
    // 교수님 피드백 #5: 문자열 대신 Enum. 참고: Device.status(DeviceStatus) 방식.
    //   기존 DB에 한글 값(정규직/근무 등)이 남아 있어도 읽히도록 JpaConverter로 양방향 변환
    //   (EnumType.STRING은 읽기 시 valueOf로 한글값에서 깨지므로 @Convert 사용).
    @Convert(converter = EmploymentType.JpaConverter.class)
    @Column(name = "employment_type", length = 20, nullable = false)
    private EmploymentType employmentType;
    @Convert(converter = WorkStatus.JpaConverter.class)
    @Column(name = "work_status", length = 20, nullable = false)
    private WorkStatus workStatus;
    @Column(name = "work_status_date")
    private LocalDate workStatusDate;
    @Column(name = "hire_date")
    private LocalDate hireDate;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    @Column(columnDefinition = "TEXT")
    private String remark;
}
