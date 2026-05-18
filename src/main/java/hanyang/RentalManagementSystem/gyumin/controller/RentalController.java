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
@RequestMapping("/api")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @GetMapping("/rentals")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getRentals(CommonSearchRequest request) {
        return ResponseEntity.ok(rentalService.getRentals(request));
    }

    @PostMapping("/rentals")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createRental(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.createRental(body));
    }

    @PatchMapping("/rentals/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateRental(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(rentalService.updateRental(id, body));
    }

    @DeleteMapping("/rentals/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @GetMapping("/users/{id}/rentals")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getUserRentals(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(rentalService.getUserRentals(id, request));
    }

    @GetMapping("/rentals/summary/by-branch")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getRentalSummaryByBranch() {
        return ResponseEntity.ok(CommonResponse.success(rentalService.getRentalSummaryByBranch()));
    }
}