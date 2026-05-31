package hanyang.RentalManagementSystem.minseok.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.common.entity.*;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    // === 7-1 ~ 7-6 부서 ===
    public CommonResponse<List<Map<String, Object>>> findAllDepartments(CommonSearchRequest request) {
        Page<Department> page = departmentRepository.findAllByIsDeletedFalse(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream().map(this::deptToMap).collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> createDepartment(Map<String, Object> body) {
        Department d = Department.builder()
                .deptName((String) body.get("deptName"))
                .createdDate(LocalDate.now())
                .appliedDate(body.get("appliedDate") != null ? LocalDate.parse((String) body.get("appliedDate")) : LocalDate.now())
                .useYn(true)
                .isDeleted(false)
                .sortOrder(body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : null)
                .build();
        departmentRepository.save(d);
        saveDeptHistory(d, "CREATE", null, toJson(d), (String) body.get("changedBy"));
        return CommonResponse.created(deptToMap(d));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> updateDepartment(Long id, Map<String, Object> body) {
        Department d = getDepartment(id);
        String before = toJson(d);
        if (body.containsKey("deptName")) d.setDeptName((String) body.get("deptName"));
        if (body.containsKey("useYn")) d.setUseYn((Boolean) body.get("useYn"));
        if (body.containsKey("appliedDate") && body.get("appliedDate") != null)
            d.setAppliedDate(LocalDate.parse((String) body.get("appliedDate")));
        if (body.containsKey("sortOrder")) d.setSortOrder(((Number) body.get("sortOrder")).intValue());
        saveDeptHistory(d, "UPDATE", before, toJson(d), (String) body.get("changedBy"));
        return CommonResponse.success(deptToMap(d));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department d = getDepartment(id);
        // [MEDIUM] existsBy 대신 findAll().isEmpty() 사용 (TeamRepository는 common/ 수정 불가)
        if (!teamRepository.findAllByDepartmentIdAndIsDeletedFalse(id).isEmpty()) {
            throw new CustomException("DEPARTMENT_HAS_TEAMS", "하위 팀이 있어 삭제할 수 없습니다.");
        }
        saveDeptHistory(d, "DELETE", toJson(d), null, null);
        d.setIsDeleted(true);
    }

    public CommonResponse<List<Map<String, Object>>> findTeamsByDepartment(Long deptId, CommonSearchRequest request) {
        getDepartment(deptId);
        Page<Team> page = teamRepository.findAllByDepartmentIdAndIsDeletedFalse(deptId, request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream().map(this::teamToMap).collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    public CommonResponse<List<Map<String, Object>>> findDepartmentHistory(Long deptId, CommonSearchRequest request) {
        Page<DeptHistory> page = deptHistoryRepository.findAllByDepartmentId(deptId, request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream().map(this::deptHistoryToMap).collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    // === 7-7 ~ 7-10 팀 ===
    @Transactional
    public CommonResponse<Map<String, Object>> createTeam(Map<String, Object> body) {
        // [HIGH] departmentId null 체크
        Object deptIdRaw = body.get("departmentId");
        if (deptIdRaw == null)
            throw new CustomException("DEPT_ID_REQUIRED", "departmentId는 필수입니다.");
        Long deptId = ((Number) deptIdRaw).longValue();
        Department dept = getDepartment(deptId);
        Team t = Team.builder()
                .department(dept)
                .teamName((String) body.get("teamName"))
                .createdDate(LocalDate.now())
                .appliedDate(body.get("appliedDate") != null ? LocalDate.parse((String) body.get("appliedDate")) : LocalDate.now())
                .useYn(true)
                .isDeleted(false)
                .sortOrder(body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : null)
                .build();
        teamRepository.save(t);
        saveTeamHistory(t, "CREATE", null, toJson(t), (String) body.get("changedBy"));
        return CommonResponse.created(teamToMap(t));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> updateTeam(Long id, Map<String, Object> body) {
        Team t = getTeam(id);
        String before = toJson(t);
        if (body.containsKey("teamName")) t.setTeamName((String) body.get("teamName"));
        if (body.containsKey("useYn")) t.setUseYn((Boolean) body.get("useYn"));
        if (body.containsKey("appliedDate") && body.get("appliedDate") != null)
            t.setAppliedDate(LocalDate.parse((String) body.get("appliedDate")));
        if (body.containsKey("sortOrder")) t.setSortOrder(((Number) body.get("sortOrder")).intValue());
        saveTeamHistory(t, "UPDATE", before, toJson(t), (String) body.get("changedBy"));
        return CommonResponse.success(teamToMap(t));
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

    public CommonResponse<List<Map<String, Object>>> findTeamHistory(Long teamId, CommonSearchRequest request) {
        Page<TeamHistory> page = teamHistoryRepository.findAllByTeamId(teamId, request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream().map(this::teamHistoryToMap).collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
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
        DeptHistory h = DeptHistory.builder()
                .department(d)
                .changeType(changeType)
                .beforeValue(before)
                .afterValue(after)
                .changedDate(LocalDate.now())
                .changedBy(changedBy)
                .build();
        deptHistoryRepository.save(h);
    }

    private void saveTeamHistory(Team t, String changeType, String before, String after, String changedBy) {
        TeamHistory h = TeamHistory.builder()
                .team(t)
                .changeType(changeType)
                .beforeValue(before)
                .afterValue(after)
                .changedDate(LocalDate.now())
                .changedBy(changedBy)
                .build();
        teamHistoryRepository.save(h);
    }

    /** [LOW] toJson 실패 시 로그 추가 */
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
                // [HIGH] getDepartment() null 안전 처리
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

    private Map<String, Object> deptToMap(Department d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("deptName", d.getDeptName());
        m.put("createdDate", d.getCreatedDate());
        m.put("appliedDate", d.getAppliedDate());
        m.put("useYn", d.getUseYn());
        m.put("sortOrder", d.getSortOrder());
        m.put("createdAt", d.getCreatedAt());
        m.put("updatedAt", d.getUpdatedAt());
        return m;
    }

    /** [HIGH] getDepartment() null 안전 처리 */
    private Map<String, Object> teamToMap(Team t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("teamName", t.getTeamName());
        Department dept = t.getDepartment();
        m.put("departmentId", dept != null ? dept.getId() : null);
        m.put("departmentName", dept != null ? dept.getDeptName() : null);
        m.put("createdDate", t.getCreatedDate());
        m.put("appliedDate", t.getAppliedDate());
        m.put("useYn", t.getUseYn());
        m.put("sortOrder", t.getSortOrder());
        return m;
    }

    private Map<String, Object> deptHistoryToMap(DeptHistory h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", h.getId());
        m.put("departmentId", h.getDepartment() != null ? h.getDepartment().getId() : null);
        m.put("changeType", h.getChangeType());
        m.put("beforeValue", h.getBeforeValue());
        m.put("afterValue", h.getAfterValue());
        m.put("changedDate", h.getChangedDate());
        m.put("changedBy", h.getChangedBy());
        m.put("createdAt", h.getCreatedAt());
        return m;
    }

    private Map<String, Object> teamHistoryToMap(TeamHistory h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", h.getId());
        m.put("teamId", h.getTeam() != null ? h.getTeam().getId() : null);
        m.put("changeType", h.getChangeType());
        m.put("beforeValue", h.getBeforeValue());
        m.put("afterValue", h.getAfterValue());
        m.put("changedDate", h.getChangedDate());
        m.put("changedBy", h.getChangedBy());
        m.put("createdAt", h.getCreatedAt());
        return m;
    }
}
