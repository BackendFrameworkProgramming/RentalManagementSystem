package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.taewoong.dto.CodeDetailResponse;
import hanyang.RentalManagementSystem.taewoong.dto.CodeDetailUpsertRequest;
import hanyang.RentalManagementSystem.taewoong.dto.CodeGroupResponse;
import hanyang.RentalManagementSystem.taewoong.dto.CodeGroupUpsertRequest;
import hanyang.RentalManagementSystem.taewoong.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
public class CommonCodeController {
    private final CommonCodeService service;

    @GetMapping
    public ResponseEntity<CommonResponse<List<Object>>> list(@RequestParam(required = false) String groupCode) {
        return ResponseEntity.ok(service.findAll(groupCode));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<CodeGroupResponse>> create(@RequestBody CodeGroupUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createGroup(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<CodeGroupResponse>> update(@PathVariable Long id, @RequestBody CodeGroupUpsertRequest request) {
        return ResponseEntity.ok(service.updateGroup(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        service.deleteGroup(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @GetMapping("/{groupId}/details")
    public ResponseEntity<CommonResponse<List<CodeDetailResponse>>> detailList(@PathVariable Long groupId) {
        return ResponseEntity.ok(service.findDetailsByGroupId(groupId));
    }

    @PostMapping("/{groupId}/details")
    public ResponseEntity<CommonResponse<CodeDetailResponse>> createDetail(
            @PathVariable Long groupId, @RequestBody CodeDetailUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDetail(groupId, request));
    }

    @PatchMapping("/details/{id}")
    public ResponseEntity<CommonResponse<CodeDetailResponse>> updateDetail(
            @PathVariable Long id, @RequestBody CodeDetailUpsertRequest request) {
        return ResponseEntity.ok(service.updateDetail(id, request));
    }

    @DeleteMapping("/details/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteDetail(@PathVariable Long id) {
        service.deleteDetail(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
