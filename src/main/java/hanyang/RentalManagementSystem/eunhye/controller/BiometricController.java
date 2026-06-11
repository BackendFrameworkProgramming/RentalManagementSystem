package hanyang.RentalManagementSystem.eunhye.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricCreateRequest;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricDetailResponse;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricListResponse;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricModelSummaryResponse;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmergencyCreateRequest;
import hanyang.RentalManagementSystem.eunhye.dto.EmergencyListResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmergencyResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmergencyUpdateRequest;
import hanyang.RentalManagementSystem.eunhye.service.BiometricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BiometricController {

    private final BiometricService biometricService;

    @GetMapping("/api/biometric-data")
    public ResponseEntity<CommonResponse<BiometricListResponse>> getBiometricDataList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(biometricService.getBiometricDataList(page, size));
    }

    // 구체 경로(/summary/by-model)를 /{id} 보다 먼저 선언해 라우트 충돌을 방지한다.
    @GetMapping("/api/biometric-data/summary/by-model")
    public ResponseEntity<CommonResponse<BiometricModelSummaryResponse>> getSummaryByModel() {
        return ResponseEntity.ok(biometricService.getSummaryByModel());
    }

    @GetMapping("/api/biometric-data/{id}")
    public ResponseEntity<CommonResponse<BiometricDetailResponse>> getBiometricDataDetail(@PathVariable Long id) {
        return ResponseEntity.ok(biometricService.getBiometricDataDetail(id));
    }

    @PostMapping("/api/biometric-data")
    public ResponseEntity<CommonResponse<BiometricResponse>> createBiometricData(@RequestBody BiometricCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(biometricService.createBiometricData(request));
    }

    @DeleteMapping("/api/biometric-data/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteBiometricData(@PathVariable Long id) {
        return ResponseEntity.ok(biometricService.deleteBiometricData(id));
    }

    @GetMapping("/api/emergency-records")
    public ResponseEntity<CommonResponse<EmergencyListResponse>> getEmergencyRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(biometricService.getEmergencyRecords(page, size));
    }

    @PostMapping("/api/emergency-records")
    public ResponseEntity<CommonResponse<EmergencyResponse>> createEmergencyRecord(@RequestBody EmergencyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(biometricService.createEmergencyRecord(request));
    }

    @PatchMapping("/api/emergency-records/{id}")
    public ResponseEntity<CommonResponse<EmergencyResponse>> updateEmergencyRecord(
            @PathVariable Long id, @RequestBody EmergencyUpdateRequest request) {
        return ResponseEntity.ok(biometricService.updateEmergencyRecord(id, request));
    }
}
