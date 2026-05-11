package hanyang.RentalManagementSystem.minseok.controller;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.member1.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    // 5-1
    @GetMapping("/api/branches")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> list(CommonSearchRequest request) {
        return ResponseEntity.ok(branchService.findAll(request));
    }

    // 5-2
    @PostMapping("/api/branches")
    public ResponseEntity<CommonResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(body));
    }

    // 5-3
    @PatchMapping("/api/branches/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(branchService.update(id, body));
    }

    // 5-4
    @DeleteMapping("/api/branches/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        branchService.delete(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // 5-5
    @GetMapping("/api/branches/{id}/managers")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> listManagers(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(branchService.findManagers(id, request));
    }

    // 5-6
    @PostMapping("/api/branch-managers")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createManager(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createManager(body));
    }

    // 5-7
    @PatchMapping("/api/branch-managers/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateManager(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(branchService.updateManager(id, body));
    }

    // 5-8
    @DeleteMapping("/api/branch-managers/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteManager(@PathVariable Long id) {
        branchService.deleteManager(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
