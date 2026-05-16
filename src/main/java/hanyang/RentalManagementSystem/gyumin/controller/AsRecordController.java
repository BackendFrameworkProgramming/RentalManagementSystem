package hanyang.RentalManagementSystem.gyumin.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.gyumin.service.AsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AsRecordController {

    private final AsRecordService asRecordService;

    // === AsRecord ===
    @GetMapping("/as-records")
    public ResponseEntity<CommonResponse<?>> getAsRecords(CommonSearchRequest request) {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.getAsRecords(request)));
    }

    @PostMapping("/as-records")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createAsRecord(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.created(asRecordService.createAsRecord(body)));
    }

    @GetMapping("/as-records/{id}")
    public ResponseEntity<CommonResponse<?>> getAsRecord(@PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.getAsRecord(id)));
    }

    @PatchMapping("/as-records/{id}")
    public ResponseEntity<CommonResponse<?>> updateAsRecord(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.updateAsRecord(id, body)));
    }

    @DeleteMapping("/as-records/{id}")
    public ResponseEntity<CommonResponse<?>> deleteAsRecord(@PathVariable Long id) {
        asRecordService.deleteAsRecord(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @GetMapping("/as-records/summary/by-vendor")
    public ResponseEntity<CommonResponse<?>> getAsSummaryByVendor() {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.getAsSummaryByVendor()));
    }

    // === Vendor ===
    @GetMapping("/vendors")
    public ResponseEntity<CommonResponse<?>> getVendors(CommonSearchRequest request) {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.getVendors(request)));
    }

    @PostMapping("/vendors")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createVendor(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.created(asRecordService.createVendor(body)));
    }

    @PatchMapping("/vendors/{id}")
    public ResponseEntity<CommonResponse<?>> updateVendor(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.updateVendor(id, body)));
    }

    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<CommonResponse<?>> deleteVendor(@PathVariable Long id) {
        asRecordService.deleteVendor(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // === AsType ===
    @GetMapping("/as-types")
    public ResponseEntity<CommonResponse<?>> getAsTypes(CommonSearchRequest request) {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.getAsTypes(request)));
    }

    @PostMapping("/as-types")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createAsType(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.created(asRecordService.createAsType(body)));
    }

    @PatchMapping("/as-types/{id}")
    public ResponseEntity<CommonResponse<?>> updateAsType(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(CommonResponse.success(asRecordService.updateAsType(id, body)));
    }

    @DeleteMapping("/as-types/{id}")
    public ResponseEntity<CommonResponse<?>> deleteAsType(@PathVariable Long id) {
        asRecordService.deleteAsType(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}