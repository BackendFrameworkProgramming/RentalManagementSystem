package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.taewoong.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
public class CommonCodeController {
    private final CommonCodeService service;

    // 코드그룹 목록 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> list(@RequestParam(required = false) String groupCode) {
        return ResponseEntity.ok(service.findAll(groupCode));
    }

    // 코드그룹 등록
    @PostMapping
    public ResponseEntity<CommonResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createGroup(body));
    }

    // 코드그룹 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.updateGroup(id, body));
    }

    // 코드그룹 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        service.deleteGroup(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    // 코드상세 목록 조회
    @GetMapping("/{groupId}/details")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> detailList(@PathVariable Long groupId) {
        return ResponseEntity.ok(service.findDetailsByGroupId(groupId));
    }

    // 코드상세 등록
    @PostMapping("/{groupId}/details")
    public ResponseEntity<CommonResponse<Map<String, Object>>> createDetail(
            @PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDetail(groupId, body));
    }

    // 코드상세 수정
    @PatchMapping("/details/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateDetail(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.updateDetail(id, body));
    }

    // 코드상세 삭제
    @DeleteMapping("/details/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteDetail(@PathVariable Long id) {
        service.deleteDetail(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}