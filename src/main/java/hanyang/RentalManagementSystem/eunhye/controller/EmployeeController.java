package hanyang.RentalManagementSystem.eunhye.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.eunhye.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/api/teams/{teamId}/employees")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getEmployeesByTeam(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(employeeService.getEmployeesByTeam(teamId, page, size));
    }

    @PostMapping("/api/employees")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createEmployee(
            @RequestBody Map<String, Object> body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(body));
    }

    @PatchMapping("/api/employees/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateEmployee(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, body));
    }

    @DeleteMapping("/api/employees/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> deleteEmployee(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(employeeService.deleteEmployee(id));
    }
}