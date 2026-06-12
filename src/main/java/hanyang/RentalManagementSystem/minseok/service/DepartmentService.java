package hanyang.RentalManagementSystem.minseok.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.Department;
import hanyang.RentalManagementSystem.common.entity.DeptHistory;
import hanyang.RentalManagementSystem.common.entity.Team;
import hanyang.RentalManagementSystem.common.entity.TeamHistory;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.DepartmentRepository;
import hanyang.RentalManagementSystem.common.repository.DeptHistoryRepository;
import hanyang.RentalManagementSystem.common.repository.EmployeeRepository;
import hanyang.RentalManagementSystem.common.repository.TeamHistoryRepository;
import hanyang.RentalManagementSystem.common.repository.TeamRepository;
import hanyang.RentalManagementSystem.minseok.dto.DepartmentResponse;
import hanyang.RentalManagementSystem.minseok.dto.DepartmentUpsertRequest;
import hanyang.RentalManagementSystem.minseok.dto.DeptHistoryResponse;
import hanyang.RentalManagementSystem.minseok.dto.TeamHistoryResponse;
import hanyang.RentalManagementSystem.minseok.dto.TeamResponse;
import hanyang.RentalManagementSystem.minseok.dto.TeamUpsertRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final DeptHistoryRepository deptHistoryRepository;
    private final TeamHistoryRepository teamHistoryRepository;
    private final EmployeeRepository employeeRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    // === 부서 ===
    public CommonResponse<List<DepartmentResponse>> findAllDepartments(CommonSearchRequest request) {
        Page<Department> page = departmentRepository.findAllByIsDeletedFalse(request.toPageable());
        return CommonResponse.success(page.getContent().stream().map(DepartmentResponse::from).toList(), Pagination.of(page));
    }

    @Transactional
    public CommonResponse<DepartmentResponse> createDepartment(DepartmentUpsertRequest req) {
        Department d = Department.builder()
                .deptName(req.getDeptName())
                .createdDate(LocalDate.now())
                .appliedDate(req.getAppliedDate() != null ? LocalDate.parse(req.getAppliedDate()) : LocalDate.now())
                .useYn(true)
                .isDeleted(false)
                .sortOrder(req.getSortOrder())
                .build();
        departmentRepository.save(d);
        saveDeptHistory(d, "CREATE", null, toJson(d), req.getChangedBy());
        return CommonResponse.created(DepartmentResponse.from(d));
    }

    @Transactional
    public CommonResponse<DepartmentResponse> updateDepartment(Long id, DepartmentUpsertRequest req) {
        Department d = getDepartment(id);
        String before = toJson(d);
        if (req.getDeptName() != null) d.setDeptName(req.getDeptName());
        if (req.getUseYn() != null) d.setUseYn(req.getUseYn());
        if (req.getAppliedDate() != null) d.setAppliedDate(LocalDate.parse(req.getAppliedDate()));
        if (req.getSortOrder() != null) d.setSortOrder(req.getSortOrder());
        saveDeptHistory(d, "UPDATE", before, toJson(d), req.getChangedBy());
        return CommonResponse.success(DepartmentResponse.from(d));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department d = getDepartment(id);
        // 교수님 #3: 전체조회 대신 exists 쿼리로 하위 팀 존재 여부 확인
        if (teamRepository.existsByDepartmentIdAndIsDeletedFalse(id)) {
            throw new CustomException("DEPARTMENT_HAS_TEAMS", "하위 팀이 있어 삭제할 수 없습니다.");
        }
        saveDeptHistory(d, "DELETE", toJson(d), null, null);
        d.setIsDeleted(true);
    }

    public CommonResponse<List<TeamResponse>> findTeamsByDepartment(Long deptId, CommonSearchRequest request) {
        getDepartment(deptId);
        Page<Team> page = teamRepository.findAllByDepartmentIdAndIsDeletedFalse(deptId, request.toPageable());
        return CommonResponse.success(page.getContent().stream().map(TeamResponse::from).toList(), Pagination.of(page));
    }

    public CommonResponse<List<DeptHistoryResponse>> findDepartmentHistory(Long deptId, CommonSearchRequest request) {
        Page<DeptHistory> page = deptHistoryRepository.findAllByDepartmentId(deptId, request.toPageable());
        return CommonResponse.success(page.getContent().stream().map(DeptHistoryResponse::from).toList(), Pagination.of(page));
    }

    // === 팀 ===
    @Transactional
    public CommonResponse<TeamResponse> createTeam(TeamUpsertRequest req) {
        if (req.getDepartmentId() == null) {
            throw new CustomException("DEPT_ID_REQUIRED", "departmentId는 필수입니다.");
        }
        Department dept = getDepartment(req.getDepartmentId());
        Team t = Team.builder()
                .department(dept)
                .teamName(req.getTeamName())
                .createdDate(LocalDate.now())
                .appliedDate(req.getAppliedDate() != null ? LocalDate.parse(req.getAppliedDate()) : LocalDate.now())
                .useYn(true)
                .isDeleted(false)
                .sortOrder(req.getSortOrder())
                .build();
        teamRepository.save(t);
        saveTeamHistory(t, "CREATE", null, toJson(t), req.getChangedBy());
        return CommonResponse.created(TeamResponse.from(t));
    }

    @Transactional
    public CommonResponse<TeamResponse> updateTeam(Long id, TeamUpsertRequest req) {
        Team t = getTeam(id);
        String before = toJson(t);
        if (req.getTeamName() != null) t.setTeamName(req.getTeamName());
        if (req.getUseYn() != null) t.setUseYn(req.getUseYn());
        if (req.getAppliedDate() != null) t.setAppliedDate(LocalDate.parse(req.getAppliedDate()));
        if (req.getSortOrder() != null) t.setSortOrder(req.getSortOrder());
        saveTeamHistory(t, "UPDATE", before, toJson(t), req.getChangedBy());
        return CommonResponse.success(TeamResponse.from(t));
    }

    @Transactional
    public void deleteTeam(Long id) {
        Team t = getTeam(id);
        if (employeeRepository.existsByTeamIdAndIsDeletedFalse(id)) {
            throw new CustomException("TEAM_HAS_EMPLOYEES", "소속 직원이 있어 삭제할 수 없습니다.");
        }
        saveTeamHistory(t, "DELETE", toJson(t), null, null);
        t.setIsDeleted(true);
    }

    public CommonResponse<List<TeamHistoryResponse>> findTeamHistory(Long teamId, CommonSearchRequest request) {
        Page<TeamHistory> page = teamHistoryRepository.findAllByTeamId(teamId, request.toPageable());
        return CommonResponse.success(page.getContent().stream().map(TeamHistoryResponse::from).toList(), Pagination.of(page));
    }

    // === 헬퍼 ===
    private Department getDepartment(Long id) {
        return departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("DEPARTMENT_NOT_FOUND", "ID " + id + "에 해당하는 부서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private Team getTeam(Long id) {
        return teamRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("TEAM_NOT_FOUND", "ID " + id + "에 해당하는 팀을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private void saveDeptHistory(Department d, String changeType, String before, String after, String changedBy) {
        deptHistoryRepository.save(DeptHistory.builder()
                .department(d).changeType(changeType).beforeValue(before).afterValue(after)
                .changedDate(LocalDate.now()).changedBy(changedBy).build());
    }

    private void saveTeamHistory(Team t, String changeType, String before, String after, String changedBy) {
        teamHistoryRepository.save(TeamHistory.builder()
                .team(t).changeType(changeType).beforeValue(before).afterValue(after)
                .changedDate(LocalDate.now()).changedBy(changedBy).build());
    }

    private String toJson(Object o) {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            if (o instanceof Department d) {
                m.put("id", d.getId());
                m.put("deptName", d.getDeptName());
                m.put("useYn", d.getUseYn());
                m.put("appliedDate", String.valueOf(d.getAppliedDate()));
                m.put("sortOrder", d.getSortOrder());
            } else if (o instanceof Team t) {
                m.put("id", t.getId());
                m.put("teamName", t.getTeamName());
                m.put("departmentId", t.getDepartment() != null ? t.getDepartment().getId() : null);
                m.put("useYn", t.getUseYn());
                m.put("appliedDate", String.valueOf(t.getAppliedDate()));
                m.put("sortOrder", t.getSortOrder());
            }
            return MAPPER.writeValueAsString(m);
        } catch (JsonProcessingException e) {
            log.warn("[DepartmentService] toJson 직렬화 실패: {}", e.getMessage());
            return "{}";
        }
    }
}
