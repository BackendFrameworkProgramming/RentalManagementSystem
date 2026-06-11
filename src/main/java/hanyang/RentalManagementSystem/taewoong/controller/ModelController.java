package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.taewoong.dto.FileUploadResponse;
import hanyang.RentalManagementSystem.taewoong.dto.ModelResponse;
import hanyang.RentalManagementSystem.taewoong.dto.ModelUpsertRequest;
import hanyang.RentalManagementSystem.taewoong.dto.ModelVersionResponse;
import hanyang.RentalManagementSystem.taewoong.dto.ModelVersionUpsertRequest;
import hanyang.RentalManagementSystem.taewoong.service.ModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @GetMapping("/api/models")
    public ResponseEntity<CommonResponse<List<ModelResponse>>> list(CommonSearchRequest request) {
        return ResponseEntity.ok(modelService.findAll(request));
    }

    @GetMapping("/api/models/{id}")
    public ResponseEntity<CommonResponse<ModelResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(modelService.findById(id));
    }

    @PostMapping("/api/models")
    public ResponseEntity<CommonResponse<ModelResponse>> create(@RequestBody ModelUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modelService.create(request));
    }

    @PatchMapping("/api/models/{id}")
    public ResponseEntity<CommonResponse<ModelResponse>> update(@PathVariable Long id, @RequestBody ModelUpsertRequest request) {
        return ResponseEntity.ok(modelService.update(id, request));
    }

    @DeleteMapping("/api/models/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        modelService.delete(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @PostMapping("/api/models/{id}/versions")
    public ResponseEntity<CommonResponse<ModelVersionResponse>> createVersion(@PathVariable Long id, @RequestBody ModelVersionUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modelService.createVersion(id, request));
    }

    @PatchMapping("/api/model-versions/{id}")
    public ResponseEntity<CommonResponse<ModelVersionResponse>> updateVersion(@PathVariable Long id, @RequestBody ModelVersionUpsertRequest request) {
        return ResponseEntity.ok(modelService.updateVersion(id, request));
    }

    @DeleteMapping("/api/model-versions/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteVersion(@PathVariable Long id) {
        modelService.deleteVersion(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @PostMapping("/api/model-versions/{id}/manual")
    public ResponseEntity<CommonResponse<FileUploadResponse>> uploadManual(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modelService.uploadManual(id, file));
    }

    @GetMapping("/api/model-versions/{id}/manual")
    public ResponseEntity<byte[]> downloadManual(@PathVariable Long id) {
        return modelService.downloadManual(id);
    }
}
