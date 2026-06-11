package hanyang.RentalManagementSystem.gyumin.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.gyumin.dto.RentalBranchSummaryResponse;
import hanyang.RentalManagementSystem.gyumin.dto.RentalCreateRequest;
import hanyang.RentalManagementSystem.gyumin.dto.RentalResponse;
import hanyang.RentalManagementSystem.gyumin.dto.RentalUpdateRequest;
import hanyang.RentalManagementSystem.gyumin.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // 1. 임대 목록 조회 (역할별 스코핑 적용)
    @GetMapping
    public ResponseEntity<CommonResponse<List<RentalResponse>>> getRentals(CommonSearchRequest request) {
        return ResponseEntity.ok(rentalService.getRentals(request));
    }

    // 2. 임대 등록(신청)
    @PostMapping
    public ResponseEntity<CommonResponse<RentalResponse>> createRental(@RequestBody RentalCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.createRental(request));
    }

    // 3. 임대 수정(상태 변경/반납 처리 등)
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<RentalResponse>> updateRental(
            @PathVariable Long id, @RequestBody RentalUpdateRequest request) {
        return ResponseEntity.ok(rentalService.updateRental(id, request));
    }

    // 4. 임대 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // 5. 특정 유저의 임대 이력 (IDOR 방어)
    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<List<RentalResponse>>> getUserRentals(
            @PathVariable Long userId, CommonSearchRequest request) {
        return ResponseEntity.ok(rentalService.getUserRentals(userId, request));
    }

    // 6. 지점별 요약
    @GetMapping("/summary/by-branch")
    public ResponseEntity<CommonResponse<List<RentalBranchSummaryResponse>>> getSummaryByBranch() {
        return ResponseEntity.ok(CommonResponse.success(rentalService.getRentalSummaryByBranch()));
    }
}
