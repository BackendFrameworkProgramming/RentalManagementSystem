package hanyang.RentalManagementSystem.gyumin.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.gyumin.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // 1. 임대 목록 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getRentals(CommonSearchRequest request) {
        return ResponseEntity.ok(rentalService.getRentals(request));
    }

    // 2. 임대 등록 (신청)
    @PostMapping
    public ResponseEntity<CommonResponse<Map<String, Object>>> createRental(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.createRental(body));
    }

    // 3. 임대 수정 (상태 변경, 반납 처리 등)
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateRental(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(rentalService.updateRental(id, body));
    }

    // 4. 임대 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // 5. 모달용: 특정 유저의 임대 이력 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getUserRentals(
            @PathVariable Long userId,
            CommonSearchRequest request) {
        return ResponseEntity.ok(rentalService.getUserRentals(userId, request));
    }

    // 6. 좌측 패널용: 지점별 요약 정보
    @GetMapping("/summary/by-branch")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getSummaryByBranch() {
        return ResponseEntity.ok(CommonResponse.success(rentalService.getRentalSummaryByBranch()));
    }
}