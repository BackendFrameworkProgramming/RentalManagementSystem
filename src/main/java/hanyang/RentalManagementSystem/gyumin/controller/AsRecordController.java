package hanyang.RentalManagementSystem.gyumin.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.gyumin.dto.AsBranchSummaryResponse;
import hanyang.RentalManagementSystem.gyumin.dto.AsRecordCreateRequest;
import hanyang.RentalManagementSystem.gyumin.dto.AsRecordResponse;
import hanyang.RentalManagementSystem.gyumin.dto.AsRecordUpdateRequest;
import hanyang.RentalManagementSystem.gyumin.service.AsRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/as-records")
@RequiredArgsConstructor
public class AsRecordController {

    private final AsRecordService asRecordService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<AsRecordResponse>>> getAsRecords(CommonSearchRequest request) {
        return ResponseEntity.ok(asRecordService.getAsRecords(request));
    }

    // 💡 상세 모달창을 띄우기 위한 단건 조회 API 완벽 복구!
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<AsRecordResponse>> getAsRecord(@PathVariable Long id) {
        return ResponseEntity.ok(asRecordService.getAsRecord(id));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<AsRecordResponse>> createAsRecord(@Valid @RequestBody AsRecordCreateRequest request) {
        return ResponseEntity.ok(asRecordService.createAsRecord(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<AsRecordResponse>> updateAsRecord(
            @PathVariable Long id, @RequestBody AsRecordUpdateRequest request) {
        return ResponseEntity.ok(asRecordService.updateAsRecord(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteAsRecord(@PathVariable Long id) {
        return ResponseEntity.ok(asRecordService.deleteAsRecord(id));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<List<AsRecordResponse>>> getUserAsRecords(
            @PathVariable Long userId, CommonSearchRequest request) {
        return ResponseEntity.ok(asRecordService.getUserAsRecords(userId, request));
    }

    @GetMapping("/summary/by-branch")
    public ResponseEntity<CommonResponse<List<AsBranchSummaryResponse>>> getSummaryByBranch() {
        return ResponseEntity.ok(asRecordService.getAsSummaryByBranch());
    }
}