package hanyang.RentalManagementSystem.minseok.controller;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.member1.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // 7-1
    @GetMapping("/api/departments")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> listDepartments(CommonSearchRequest request) {
        return ResponseEntity.ok(departmentService.findAllDepartments(request));
    }

    // 7-2
    @PostMapping("/api/departments")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createDepartment(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(body));
    }

    // 7-3
    @PatchMapping("/api/departments/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateDepartment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, body));
    }

    // 7-4
    @DeleteMapping("/api/departments/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // 7-5
    @GetMapping("/api/departments/{id}/teams")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> teamsByDepartment(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(departmentService.findTeamsByDepartment(id, request));
    }

    // 7-6
    @GetMapping("/api/departments/{id}/history")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> departmentHistory(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(departmentService.findDepartmentHistory(id, request));
    }

    // 7-7
    @PostMapping("/api/teams")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createTeam(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createTeam(body));
    }

    // 7-8
    @PatchMapping("/api/teams/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateTeam(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(departmentService.updateTeam(id, body));
    }

    // 7-9
    @DeleteMapping("/api/teams/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteTeam(@PathVariable Long id) {
        departmentService.deleteTeam(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // 7-10
    @GetMapping("/api/teams/{id}/history")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> teamHistory(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(departmentService.findTeamHistory(id, request));
    }
}
