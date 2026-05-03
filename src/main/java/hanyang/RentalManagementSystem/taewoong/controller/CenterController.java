package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.entity.Center;
import hanyang.RentalManagementSystem.taewoong.service.CenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/center")
@RequiredArgsConstructor
public class CenterController {
    private final CenterService centerService;

    @GetMapping
    public ResponseEntity<CommonResponse<Map<String, Object>>> get() {
        return ResponseEntity.ok(centerService.get());
    }

    @PutMapping
    public ResponseEntity<CommonResponse<Map<String, Object>>> save(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(centerService.save(body));
    }
}
