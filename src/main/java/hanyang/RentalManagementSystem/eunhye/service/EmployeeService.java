package hanyang.RentalManagementSystem.eunhye.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.Employee;
import hanyang.RentalManagementSystem.common.entity.Team;
import hanyang.RentalManagementSystem.common.enums.EmploymentType;
import hanyang.RentalManagementSystem.common.enums.WorkStatus;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.EmployeeRepository;
import hanyang.RentalManagementSystem.common.repository.TeamRepository;
import hanyang.RentalManagementSystem.eunhye.dto.EmployeeListResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmployeeResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmployeeUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public CommonResponse<EmployeeListResponse> getEmployeesByTeam(Long teamId, int page, int size) {
        Page<Employee> employees = employeeRepository.findAllByTeamIdAndIsDeletedFalse(teamId, PageRequest.of(page - 1, size));
        EmployeeListResponse data = EmployeeListResponse.builder()
                .employees(employees.getContent().stream().map(EmployeeResponse::from).toList())
                .build();
        return CommonResponse.success(data, Pagination.of(employees));
    }

    public CommonResponse<EmployeeResponse> createEmployee(EmployeeUpsertRequest req) {
        if (req.getTeamId() == null) {
            throw new CustomException("INVALID_REQUEST", "teamId는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        Team team = teamRepository.findByIdAndIsDeletedFalse(req.getTeamId())
                .orElseThrow(() -> new CustomException("TEAM_NOT_FOUND", "팀을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Employee employee = Employee.builder()
                .team(team)
                .department(team.getDepartment())
                .empName(req.getEmpName())
                .empNo(req.getEmpNo())
                .jobTitle(req.getJobTitle())
                .employmentType(parseEmploymentType(req.getEmploymentType()))
                .workStatus(parseWorkStatus(req.getWorkStatus()))
                .workStatusDate(LocalDate.now())
                .hireDate(parseDate(req.getHireDate()))
                .remark(req.getRemark())
                .isDeleted(false)
                .build();
        employeeRepository.save(employee);
        return CommonResponse.created(EmployeeResponse.from(employee));
    }

    public CommonResponse<EmployeeResponse> updateEmployee(Long id, EmployeeUpsertRequest req) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .orElseThrow(() -> new CustomException("EMPLOYEE_NOT_FOUND", "직원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (req.getTeamId() != null) {
            Team team = teamRepository.findByIdAndIsDeletedFalse(req.getTeamId())
                    .orElseThrow(() -> new CustomException("TEAM_NOT_FOUND", "팀을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
            employee.setTeam(team);
            employee.setDepartment(team.getDepartment());
        }
        if (req.getEmpName() != null) employee.setEmpName(req.getEmpName());
        if (req.getEmpNo() != null) employee.setEmpNo(req.getEmpNo());
        if (req.getJobTitle() != null) employee.setJobTitle(req.getJobTitle());
        if (req.getEmploymentType() != null) employee.setEmploymentType(parseEmploymentType(req.getEmploymentType()));
        if (req.getWorkStatus() != null) {
            employee.setWorkStatus(parseWorkStatus(req.getWorkStatus()));
            employee.setWorkStatusDate(LocalDate.now());
        }
        if (req.getHireDate() != null) employee.setHireDate(parseDate(req.getHireDate()));
        if (req.getRemark() != null) employee.setRemark(req.getRemark());
        return CommonResponse.success(EmployeeResponse.from(employee));
    }

    public CommonResponse<Void> deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .orElseThrow(() -> new CustomException("EMPLOYEE_NOT_FOUND", "직원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        employee.setIsDeleted(true);
        return CommonResponse.success(null);
    }

    private EmploymentType parseEmploymentType(String v) {
        try {
            return EmploymentType.fromString(v);
        } catch (IllegalArgumentException e) {
            throw new CustomException("INVALID_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private WorkStatus parseWorkStatus(String v) {
        try {
            return WorkStatus.fromString(v);
        } catch (IllegalArgumentException e) {
            throw new CustomException("INVALID_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private LocalDate parseDate(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new CustomException("INVALID_REQUEST", "날짜 형식이 올바르지 않습니다(YYYY-MM-DD): " + v, HttpStatus.BAD_REQUEST);
        }
    }
}
