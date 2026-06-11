package hanyang.RentalManagementSystem.minseok.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.minseok.dto.BranchManagerResponse;
import hanyang.RentalManagementSystem.minseok.dto.BranchManagerUpsertRequest;
import hanyang.RentalManagementSystem.minseok.dto.BranchResponse;
import hanyang.RentalManagementSystem.minseok.dto.BranchUpsertRequest;
import hanyang.RentalManagementSystem.minseok.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/api/branches")
    public ResponseEntity<CommonResponse<List<BranchResponse>>> list(CommonSearchRequest request) {
        return ResponseEntity.ok(branchService.findAll(request));
    }

    @PostMapping("/api/branches")
    public ResponseEntity<CommonResponse<BranchResponse>> create(@RequestBody BranchUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(request));
    }

    @PatchMapping("/api/branches/{id}")
    public ResponseEntity<CommonResponse<BranchResponse>> update(@PathVariable Long id, @RequestBody BranchUpsertRequest request) {
        return ResponseEntity.ok(branchService.update(id, request));
    }

    @DeleteMapping("/api/branches/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        branchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/branches/{id}/managers")
    public ResponseEntity<CommonResponse<List<BranchManagerResponse>>> listManagers(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(branchService.findManagers(id, request));
    }

    @PostMapping("/api/branch-managers")
    public ResponseEntity<CommonResponse<BranchManagerResponse>> createManager(@RequestBody BranchManagerUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createManager(request));
    }

    @PatchMapping("/api/branch-managers/{id}")
    public ResponseEntity<CommonResponse<BranchManagerResponse>> updateManager(@PathVariable Long id, @RequestBody BranchManagerUpsertRequest request) {
        return ResponseEntity.ok(branchService.updateManager(id, request));
    }

    @DeleteMapping("/api/branch-managers/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable Long id) {
        branchService.deleteManager(id);
        return ResponseEntity.noContent().build();
    }
}
