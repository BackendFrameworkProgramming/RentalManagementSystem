package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.taewoong.dto.BatchBranchLinkRequest;
import hanyang.RentalManagementSystem.taewoong.dto.BatchDeviceIdsRequest;
import hanyang.RentalManagementSystem.taewoong.dto.BranchLinkRequest;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceAsHistoryResponse;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceCreateRequest;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceResponse;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceStatusUpdateRequest;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceSummaryResponse;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceUpdateRequest;
import hanyang.RentalManagementSystem.taewoong.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<DeviceResponse>>> list(CommonSearchRequest request) {
        return ResponseEntity.ok(deviceService.findAll(request));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<DeviceResponse>> create(@RequestBody DeviceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<DeviceResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<DeviceResponse>> update(@PathVariable Long id, @RequestBody DeviceUpdateRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CommonResponse<DeviceResponse>> updateStatus(
            @PathVariable Long id, @RequestBody DeviceStatusUpdateRequest request) {
        return ResponseEntity.ok(deviceService.updateStatus(id, request.getStatus()));
    }

    @GetMapping("/{id}/as-records")
    public ResponseEntity<CommonResponse<List<DeviceAsHistoryResponse>>> asHistory(
            @PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(deviceService.findAsRecordsByDeviceId(id, request));
    }

    @PatchMapping("/{id}/branch")
    public ResponseEntity<CommonResponse<DeviceResponse>> linkBranch(
            @PathVariable Long id, @RequestBody BranchLinkRequest request) {
        return ResponseEntity.ok(deviceService.linkBranch(id, request.getBranchId()));
    }

    @PatchMapping("/batch/branch")
    public ResponseEntity<CommonResponse<Map<String, Object>>> batchLinkBranch(@RequestBody BatchBranchLinkRequest request) {
        return ResponseEntity.ok(deviceService.batchLinkBranch(request.getDeviceIds(), request.getBranchId()));
    }

    @DeleteMapping("/{id}/branch")
    public ResponseEntity<CommonResponse<Void>> unlinkBranch(@PathVariable Long id) {
        deviceService.unlinkBranch(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @PatchMapping("/batch/branch-release")
    public ResponseEntity<CommonResponse<Map<String, Object>>> batchUnlinkBranch(@RequestBody BatchDeviceIdsRequest request) {
        return ResponseEntity.ok(deviceService.batchUnlinkBranch(request.getDeviceIds()));
    }

    @GetMapping("/summary/by-branch")
    public ResponseEntity<CommonResponse<DeviceSummaryResponse>> summaryByBranch() {
        return ResponseEntity.ok(deviceService.summaryByBranch());
    }

    @GetMapping("/count/by-model-version/{mvId}")
    public ResponseEntity<Map<String, Object>> countByModelVersion(@PathVariable Long mvId) {
        return ResponseEntity.ok(Map.of("modelVersionId", mvId, "count", deviceService.countByModelVersionId(mvId)));
    }
}
