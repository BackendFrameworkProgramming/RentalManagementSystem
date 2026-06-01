package hanyang.RentalManagementSystem.gyumin.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.gyumin.service.AsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/as-records")
@RequiredArgsConstructor
public class AsRecordController {

    private final AsRecordService asRecordService;

    // 1. A/S 목록 조회 (기획서 메인 그리드)
    @GetMapping
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getAsRecords(CommonSearchRequest request) {
        return ResponseEntity.ok(asRecordService.getAsRecords(request));
    }

    // 2. A/S 등록 (신청)
    @PostMapping
    public ResponseEntity<CommonResponse<Map<String, Object>>> createAsRecord(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asRecordService.createAsRecord(body));
    }

    // 3. A/S 수정 (상태 변경 등)
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateAsRecord(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(asRecordService.updateAsRecord(id, body));
    }

    // 4. A/S 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteAsRecord(@PathVariable Long id) {
        asRecordService.deleteAsRecord(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // 5. 모달용: 특정 유저의 A/S 이력 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getUserAsRecords(
            @PathVariable Long userId,
            CommonSearchRequest request) {
        return ResponseEntity.ok(asRecordService.getUserAsRecords(userId, request));
    }

    // 6. 좌측 패널용: 지점별 A/S 요약 (총 건수 / 진행중 건수)
    @GetMapping("/summary/by-branch")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getSummaryByBranch() {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.getAsSummaryByBranch()));
    }
}