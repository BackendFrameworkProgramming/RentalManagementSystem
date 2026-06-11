package hanyang.RentalManagementSystem.gyumin.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.gyumin.dto.AsBranchSummaryResponse;
import hanyang.RentalManagementSystem.gyumin.dto.AsRecordCreateRequest;
import hanyang.RentalManagementSystem.gyumin.dto.AsRecordResponse;
import hanyang.RentalManagementSystem.gyumin.dto.AsRecordUpdateRequest;
import hanyang.RentalManagementSystem.gyumin.service.AsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/as-records")
@RequiredArgsConstructor
public class AsRecordController {

    private final AsRecordService asRecordService;

    // 1. A/S 목록 조회 (역할별 스코핑 적용)
    @GetMapping
    public ResponseEntity<CommonResponse<List<AsRecordResponse>>> getAsRecords(CommonSearchRequest request) {
        return ResponseEntity.ok(asRecordService.getAsRecords(request));
    }

    // 2. A/S 등록(신청)
    @PostMapping
    public ResponseEntity<CommonResponse<AsRecordResponse>> createAsRecord(@RequestBody AsRecordCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asRecordService.createAsRecord(request));
    }

    // 3. A/S 수정(상태 변경 등)
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<AsRecordResponse>> updateAsRecord(
            @PathVariable Long id, @RequestBody AsRecordUpdateRequest request) {
        return ResponseEntity.ok(asRecordService.updateAsRecord(id, request));
    }

    // 4. A/S 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteAsRecord(@PathVariable Long id) {
        asRecordService.deleteAsRecord(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // 5. 특정 유저의 A/S 이력 (IDOR 방어)
    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<List<AsRecordResponse>>> getUserAsRecords(
            @PathVariable Long userId, CommonSearchRequest request) {
        return ResponseEntity.ok(asRecordService.getUserAsRecords(userId, request));
    }

    // 6. 지점별 A/S 요약
    @GetMapping("/summary/by-branch")
    public ResponseEntity<CommonResponse<List<AsBranchSummaryResponse>>> getSummaryByBranch() {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.getAsSummaryByBranch()));
    }
}
