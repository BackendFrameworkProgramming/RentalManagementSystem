package hanyang.RentalManagementSystem.eunhye.dto;

import hanyang.RentalManagementSystem.common.entity.Department;
import hanyang.RentalManagementSystem.common.entity.Employee;
import hanyang.RentalManagementSystem.common.entity.Team;
import lombok.*;

import java.time.LocalDate;

/** 직원 응답. 기존 Map 키 유지(리플렉션 제거). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeResponse {
    private Long id;
    private Long teamId;
    private Long departmentId;
    private String departmentName;
    private String teamName;
    private String empName;
    private String empNo;
    private String jobTitle;
    private String employmentType;
    private String workStatus;
    private LocalDate workStatusDate;
    private LocalDate hireDate;
    private String remark;

    public static EmployeeResponse from(Employee e) {
        Team team = e.getTeam();
        Department dept = e.getDepartment();
        return EmployeeResponse.builder()
                .id(e.getId())
                .teamId(team != null ? team.getId() : null)
                .departmentId(dept != null ? dept.getId() : null)
                .departmentName(dept != null ? dept.getDeptName() : null)
                .teamName(team != null ? team.getTeamName() : null)
                .empName(e.getEmpName())
                .empNo(e.getEmpNo())
                .jobTitle(e.getJobTitle())
                .employmentType(e.getEmploymentType())
                .workStatus(e.getWorkStatus())
                .workStatusDate(e.getWorkStatusDate())
                .hireDate(e.getHireDate())
                .remark(e.getRemark())
                .build();
    }
}
