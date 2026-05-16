package hanyang.RentalManagementSystem.gyumin.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.gyumin.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @GetMapping("/rentals")
    public ResponseEntity<CommonResponse<?>> getRentals(CommonSearchRequest request) {
        return ResponseEntity.ok(CommonResponse.success(rentalService.getRentals(request)));
    }

    @PostMapping("/rentals")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createRental(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.created(rentalService.createRental(body)));
    }

    @PatchMapping("/rentals/{id}")
    public ResponseEntity<CommonResponse<?>> updateRental(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(CommonResponse.success(rentalService.updateRental(id, body)));
    }

    @DeleteMapping("/rentals/{id}")
    public ResponseEntity<CommonResponse<?>> deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @GetMapping("/users/{id}/rentals")
    public ResponseEntity<CommonResponse<?>> getUserRentals(@PathVariable Long id, CommonSearchRequest request) {
        return ResponseEntity.ok(CommonResponse.success(rentalService.getUserRentals(id, request)));
    }

    @GetMapping("/branches/{id}/available-devices")
    public ResponseEntity<CommonResponse<?>> getAvailableDevices(@PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success(rentalService.getAvailableDevicesByBranch(id)));
    }

    @GetMapping("/rentals/summary/by-branch")
    public ResponseEntity<CommonResponse<?>> getRentalSummaryByBranch() {
        return ResponseEntity.ok(CommonResponse.success(rentalService.getRentalSummaryByBranch()));
    }
}