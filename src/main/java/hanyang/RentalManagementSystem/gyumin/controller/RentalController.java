package hanyang.RentalManagementSystem.gyumin.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.gyumin.dto.RentalBranchSummaryResponse;
import hanyang.RentalManagementSystem.gyumin.dto.RentalCreateRequest;
import hanyang.RentalManagementSystem.gyumin.dto.RentalResponse;
import hanyang.RentalManagementSystem.gyumin.dto.RentalUpdateRequest;
import hanyang.RentalManagementSystem.gyumin.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<RentalResponse>>> getRentals(CommonSearchRequest request) {
        return ResponseEntity.ok(rentalService.getRentals(request));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<RentalResponse>> createRental(@Valid @RequestBody RentalCreateRequest request) {
        return ResponseEntity.ok(rentalService.createRental(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<RentalResponse>> updateRental(
            @PathVariable Long id, @RequestBody RentalUpdateRequest request) {
        return ResponseEntity.ok(rentalService.updateRental(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteRental(@PathVariable Long id) {
        return ResponseEntity.ok(rentalService.deleteRental(id));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<List<RentalResponse>>> getUserRentals(
            @PathVariable Long userId, CommonSearchRequest request) {
        return ResponseEntity.ok(rentalService.getUserRentals(userId, request));
    }

    @GetMapping("/summary/by-branch")
    public ResponseEntity<CommonResponse<List<RentalBranchSummaryResponse>>> getSummaryByBranch() {
        return ResponseEntity.ok(CommonResponse.success(rentalService.getRentalSummaryByBranch()));
    }
}