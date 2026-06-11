package hanyang.RentalManagementSystem.minseok.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.minseok.dto.DepartmentResponse;
import hanyang.RentalManagementSystem.minseok.dto.DepartmentUpsertRequest;
import hanyang.RentalManagementSystem.minseok.dto.DeptHistoryResponse;
import hanyang.RentalManagementSystem.minseok.dto.TeamHistoryResponse;
import hanyang.RentalManagementSystem.minseok.dto.TeamResponse;
import hanyang.RentalManagementSystem.minseok.dto.TeamUpsertRequest;
import hanyang.RentalManagementSystem.minseok.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/api/departments")
    public ResponseEntity<CommonResponse<List<DepartmentResponse>>> listDepartments(CommonSearchRequest request) {
        return ResponseEntity.ok(departmentService.findAllDepartments(request));
    }

    @PostMapping("/api/departments")
    public ResponseEntity<CommonResponse<DepartmentResponse>> createDepartment(@RequestBody DepartmentUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(request));
    }

    @PatchMapping("/api/departments/{id}")
    public ResponseEntity<CommonResponse<DepartmentResponse>> updateDepartment(@PathVariable Long id, @RequestBody DepartmentUpsertRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/api/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/departments/{id}/teams")
    public ResponseEntity<CommonResponse<List<TeamResponse>>> teamsByDepartment(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(departmentService.findTeamsByDepartment(id, request));
    }

    @GetMapping("/api/departments/{id}/history")
    public ResponseEntity<CommonResponse<List<DeptHistoryResponse>>> departmentHistory(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(departmentService.findDepartmentHistory(id, request));
    }

    @PostMapping("/api/teams")
    public ResponseEntity<CommonResponse<TeamResponse>> createTeam(@RequestBody TeamUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createTeam(request));
    }

    @PatchMapping("/api/teams/{id}")
    public ResponseEntity<CommonResponse<TeamResponse>> updateTeam(@PathVariable Long id, @RequestBody TeamUpsertRequest request) {
        return ResponseEntity.ok(departmentService.updateTeam(id, request));
    }

    @DeleteMapping("/api/teams/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        departmentService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/teams/{id}/history")
    public ResponseEntity<CommonResponse<List<TeamHistoryResponse>>> teamHistory(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(departmentService.findTeamHistory(id, request));
    }
}
