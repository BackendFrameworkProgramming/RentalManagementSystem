package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.taewoong.dto.CenterResponse;
import hanyang.RentalManagementSystem.taewoong.dto.CenterUpsertRequest;
import hanyang.RentalManagementSystem.taewoong.service.CenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/center")
@RequiredArgsConstructor
public class CenterController {
    private final CenterService centerService;

    @GetMapping
    public ResponseEntity<CommonResponse<CenterResponse>> get() {
        return ResponseEntity.ok(centerService.get());
    }

    @PutMapping
    public ResponseEntity<CommonResponse<CenterResponse>> save(@RequestBody CenterUpsertRequest request) {
        return ResponseEntity.ok(centerService.save(request));
    }
}
