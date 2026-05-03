package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.taewoong.service.ModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    // M-1
    @GetMapping("/api/models")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> list(CommonSearchRequest request) {
        return ResponseEntity.ok(modelService.findAll(request));
    }

    // M-2
    @GetMapping("/api/models/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(modelService.findById(id));
    }

    // M-3
    @PostMapping("/api/models")
    public ResponseEntity<CommonResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modelService.create(body));
    }

    // M-4
    @PatchMapping("/api/models/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(modelService.update(id, body));
    }

    // M-5
    @DeleteMapping("/api/models/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        modelService.delete(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // M-6
    @PostMapping("/api/models/{id}/versions")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createVersion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modelService.createVersion(id, body));
    }

    // M-7
    @PatchMapping("/api/model-versions/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateVersion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(modelService.updateVersion(id, body));
    }

    // M-8
    @DeleteMapping("/api/model-versions/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteVersion(@PathVariable Long id) {
        modelService.deleteVersion(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // M-9
    @PostMapping("/api/model-versions/{id}/manual")
    public ResponseEntity<CommonResponse<Map<String, Object>>> uploadManual(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modelService.uploadManual(id, file));
    }

    // M-10
    @GetMapping("/api/model-versions/{id}/manual")
    public ResponseEntity<byte[]> downloadManual(@PathVariable Long id) {
        return modelService.downloadManual(id);
    }
}
