package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.taewoong.dto.DesignHistoryResponse;
import hanyang.RentalManagementSystem.taewoong.dto.DesignHistoryUpsertRequest;
import hanyang.RentalManagementSystem.taewoong.service.DesignHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/design-history")
@RequiredArgsConstructor
public class DesignHistoryController {
    private final DesignHistoryService service;

    @GetMapping
    public ResponseEntity<CommonResponse<List<DesignHistoryResponse>>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<DesignHistoryResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<DesignHistoryResponse>> create(@RequestBody DesignHistoryUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<DesignHistoryResponse>> update(@PathVariable Long id, @RequestBody DesignHistoryUpsertRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
