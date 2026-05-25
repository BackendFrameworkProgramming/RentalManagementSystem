package hanyang.RentalManagementSystem.eunhye.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.Employee;
import hanyang.RentalManagementSystem.common.entity.Team;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.EmployeeRepository;
import hanyang.RentalManagementSystem.common.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public CommonResponse<Map<String, Object>> getEmployeesByTeam(Long teamId, int page, int size) {
        Page<Employee> employees = employeeRepository.findAllByTeamIdAndIsDeletedFalse(
                teamId,
                PageRequest.of(page - 1, size)
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("employees", employees.getContent().stream().map(this::toMap).toList());

        return CommonResponse.success(data, Pagination.of(employees));
    }

    public CommonResponse<Map<String, Object>> createEmployee(Map<String, Object> body) {
        Long teamId = toLong(body.get("teamId"));

        Team team = teamRepository.findByIdAndIsDeletedFalse(teamId)
                .orElseThrow(() -> new CustomException(
                        "TEAM_NOT_FOUND",
                        "팀을 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        Employee employee = Employee.builder()
                .team(team)
                .department(team.getDepartment())
                .empName(toString(body.get("empName")))
                .empNo(toString(body.get("empNo")))
                .jobTitle(toString(body.get("jobTitle")))
                .employmentType(toString(body.get("employmentType")))
                .workStatus(toString(body.get("workStatus")))
                .workStatusDate(LocalDate.now())
                .hireDate(toLocalDate(body.get("hireDate")))
                .remark(toString(body.get("remark")))
                .isDeleted(false)
                .build();

        Employee saved = employeeRepository.save(employee);

        return CommonResponse.created(toMap(saved));
    }

    public CommonResponse<Map<String, Object>> updateEmployee(Long id, Map<String, Object> body) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .orElseThrow(() -> new CustomException(
                        "EMPLOYEE_NOT_FOUND",
                        "직원을 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        if (body.containsKey("teamId")) {
            Team team = teamRepository.findByIdAndIsDeletedFalse(toLong(body.get("teamId")))
                    .orElseThrow(() -> new CustomException(
                            "TEAM_NOT_FOUND",
                            "팀을 찾을 수 없습니다.",
                            HttpStatus.NOT_FOUND
                    ));

            employee.setTeam(team);
            employee.setDepartment(team.getDepartment());
        }

        if (body.containsKey("empName")) {
            employee.setEmpName(toString(body.get("empName")));
        }

        if (body.containsKey("empNo")) {
            employee.setEmpNo(toString(body.get("empNo")));
        }

        if (body.containsKey("jobTitle")) {
            employee.setJobTitle(toString(body.get("jobTitle")));
        }

        if (body.containsKey("employmentType")) {
            employee.setEmploymentType(toString(body.get("employmentType")));
        }

        if (body.containsKey("workStatus")) {
            employee.setWorkStatus(toString(body.get("workStatus")));
            employee.setWorkStatusDate(LocalDate.now());
        }

        if (body.containsKey("hireDate")) {
            employee.setHireDate(toLocalDate(body.get("hireDate")));
        }

        if (body.containsKey("remark")) {
            employee.setRemark(toString(body.get("remark")));
        }

        return CommonResponse.success(toMap(employee));
    }

    public CommonResponse<Map<String, Object>> deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .orElseThrow(() -> new CustomException(
                        "EMPLOYEE_NOT_FOUND",
                        "직원을 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        employee.setIsDeleted(true);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("message", "직원이 삭제되었습니다.");

        return CommonResponse.success(data);
    }

    private Map<String, Object> toMap(Employee employee) {
        Map<String, Object> map = new LinkedHashMap<>();

        Team team = employee.getTeam();
        Object department = employee.getDepartment();

        map.put("id", employee.getId());

        map.put("teamId", team != null ? team.getId() : null);
        map.put("departmentId", department != null ? callGetter(department, "getId") : null);

        map.put("departmentName", getDepartmentName(department));
        map.put("teamName", getTeamName(team));

        map.put("empName", employee.getEmpName());
        map.put("empNo", employee.getEmpNo());
        map.put("jobTitle", employee.getJobTitle());
        map.put("employmentType", employee.getEmploymentType());
        map.put("workStatus", employee.getWorkStatus());
        map.put("workStatusDate", employee.getWorkStatusDate());
        map.put("hireDate", employee.getHireDate());
        map.put("remark", employee.getRemark());

        return map;
    }

    private String getDepartmentName(Object department) {
        String name = firstNonBlank(
                callGetterAsString(department, "getDepartmentName"),
                callGetterAsString(department, "getDeptName"),
                callGetterAsString(department, "getName")
        );

        return name != null ? name : "-";
    }

    private String getTeamName(Team team) {
        String name = firstNonBlank(
                callGetterAsString(team, "getTeamName"),
                callGetterAsString(team, "getName")
        );

        return name != null ? name : "-";
    }

    private Object callGetter(Object target, String methodName) {
        try {
            if (target == null) return null;

            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private String callGetterAsString(Object target, String methodName) {
        Object value = callGetter(target, methodName);
        return value == null ? null : value.toString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private Long toLong(Object value) {
        if (value == null || value.toString().isBlank()) {
            throw new CustomException("INVALID_REQUEST", "필수 값이 누락되었습니다.");
        }

        return Long.valueOf(value.toString());
    }

    private String toString(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDate toLocalDate(Object value) {
        return value == null || value.toString().isBlank()
                ? null
                : LocalDate.parse(value.toString());
    }
}