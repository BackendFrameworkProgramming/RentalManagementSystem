package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.taewoong.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // 1-1 디바이스 목록 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> list(CommonSearchRequest request) {
        return ResponseEntity.ok(deviceService.findAll(request));
    }

    // 1-2 디바이스 신규 등록
    @PostMapping
    public ResponseEntity<CommonResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.create(body));
    }

    // 1-3 디바이스 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.findById(id));
    }

    // 1-4 디바이스 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(deviceService.update(id, body));
    }

    // 1-5 디바이스 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateStatus(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(deviceService.updateStatus(id, (String) body.get("status")));
    }

    // 1-6 디바이스 AS 이력 조회
    @GetMapping("/{id}/as-records")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> asHistory(
            @PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(deviceService.findAsRecordsByDeviceId(id, request));
    }

    // 1-7 지점 연결 (단건)
    @PatchMapping("/{id}/branch")
    public ResponseEntity<CommonResponse<Map<String, Object>>> linkBranch(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(deviceService.linkBranch(id, (Long) body.get("branchId")));
    }

    // 1-8 지점 연결 (다중)
    @PatchMapping("/batch/branch")
    public ResponseEntity<CommonResponse<Map<String, Object>>> batchLinkBranch(@RequestBody Map<String, Object> body) {
        List<Long> deviceIds = ((List<?>) body.get("deviceIds")).stream()
                .map(o -> ((Number) o).longValue())
                .collect(Collectors.toList());
        Long branchId = ((Number) body.get("branchId")).longValue();
        return ResponseEntity.ok(deviceService.batchLinkBranch(deviceIds, branchId));
    }

    // 1-9 지점 해제 (단건)
    @DeleteMapping("/{id}/branch")
    public ResponseEntity<CommonResponse<Void>> unlinkBranch(@PathVariable Long id) {
        deviceService.unlinkBranch(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // 1-10 지점 해제 (다중)
    @PatchMapping("/batch/branch-release")
    public ResponseEntity<CommonResponse<Map<String, Object>>> batchUnlinkBranch(@RequestBody Map<String, Object> body) {
        List<Long> deviceIds = ((List<?>) body.get("deviceIds")).stream()
                .map(o -> ((Number) o).longValue())
                .collect(Collectors.toList());
        return ResponseEntity.ok(deviceService.batchUnlinkBranch(deviceIds));
    }

    // 1-11 지점별 수량 집계
    @GetMapping("/summary/by-branch")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> summaryByBranch() {
        return ResponseEntity.ok(CommonResponse.success(deviceService.summaryByBranch()));
    }
}
