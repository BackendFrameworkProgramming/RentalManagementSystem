package hanyang.RentalManagementSystem.eunhye.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmployeeListResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmployeeResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmployeeUpsertRequest;
import hanyang.RentalManagementSystem.eunhye.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/api/teams/{teamId}/employees")
    public ResponseEntity<CommonResponse<EmployeeListResponse>> getEmployeesByTeam(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(employeeService.getEmployeesByTeam(teamId, page, size));
    }

    @PostMapping("/api/employees")
    public ResponseEntity<CommonResponse<EmployeeResponse>> createEmployee(@RequestBody EmployeeUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @PatchMapping("/api/employees/{id}")
    public ResponseEntity<CommonResponse<EmployeeResponse>> updateEmployee(@PathVariable Long id, @RequestBody EmployeeUpsertRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/api/employees/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.deleteEmployee(id));
    }
}
