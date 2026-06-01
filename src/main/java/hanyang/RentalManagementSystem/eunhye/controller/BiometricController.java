package hanyang.RentalManagementSystem.eunhye.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.eunhye.service.BiometricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BiometricController {

    private final BiometricService biometricService;

    @GetMapping("/api/biometric-data")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getBiometricDataList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(biometricService.getBiometricDataList(page, size));
    }

    // 구체 경로(/summary/by-model)를 /{id} 보다 먼저 선언해 라우트 충돌을 방지한다.
    @GetMapping("/api/biometric-data/summary/by-model")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getSummaryByModel() {
        return ResponseEntity.ok(biometricService.getSummaryByModel());
    }

    @GetMapping("/api/biometric-data/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getBiometricDataDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(biometricService.getBiometricDataDetail(id));
    }

    @PostMapping("/api/biometric-data")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createBiometricData(
            @RequestBody Map<String, Object> body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(biometricService.createBiometricData(body));
    }

    @DeleteMapping("/api/biometric-data/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> deleteBiometricData(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(biometricService.deleteBiometricData(id));
    }

    @GetMapping("/api/emergency-records")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getEmergencyRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(biometricService.getEmergencyRecords(page, size));
    }

    @PostMapping("/api/emergency-records")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createEmergencyRecord(
            @RequestBody Map<String, Object> body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(biometricService.createEmergencyRecord(body));
    }

    @PatchMapping("/api/emergency-records/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateEmergencyRecord(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        return ResponseEntity.ok(biometricService.updateEmergencyRecord(id, body));
    }
}