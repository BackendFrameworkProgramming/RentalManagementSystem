package hanyang.RentalManagementSystem.gyumin.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.AsRecord;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.AsRecordRepository;
import hanyang.RentalManagementSystem.common.repository.BranchRepository;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, rollbackFor = Exception.class) // [HIGH] Checked Exception 롤백 보장
public class AsRecordService {

    private final AsRecordRepository asRecordRepository;
    private final DeviceRepository deviceRepository;
    private final RentalRepository rentalRepository;
    private final BranchRepository branchRepository;

    // [HIGH] 상태 전이 검증 로직
    private void validateStatusTransition(String current, String next) {
        var allowed = Map.of(
                "AS_RECEIVED", List.of("AS_PROGRESS", "AS_COMPLETED"),
                "AS_PROGRESS", List.of("AS_COMPLETED")
        );
        var validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new CustomException("INVALID_STATUS_TRANSITION", current + " 에서 " + next + "(으)로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    private Map<String, Object> convertToMap(AsRecord asRecord) {
        var map = new HashMap<String, Object>();
        map.put("id", asRecord.getId());
        map.put("status", asRecord.getStatus());
        map.put("requestDate", asRecord.getReceiptDate());
        map.put("completionDate", asRecord.getCompleteDate());
        map.put("asDescription", asRecord.getReceiptContent() != null ? asRecord.getReceiptContent() : "-");

        if (asRecord.getDevice() != null) {
            map.put("deviceId", asRecord.getDevice().getDeviceId());
            var modelName = (asRecord.getDevice().getModelVersion() != null && asRecord.getDevice().getModelVersion().getModel() != null)
                    ? asRecord.getDevice().getModelVersion().getModel().getModelName() : "-";
            map.put("modelName", modelName);
        } else {
            map.put("deviceId", "-"); map.put("modelName", "-");
        }

        if (asRecord.getBranch() != null) {
            map.put("branchName", asRecord.getBranch().getBranchName());
            map.put("branchId", asRecord.getBranch().getId());
        } else {
            map.put("branchName", "-"); map.put("branchId", null);
        }

        if (asRecord.getRental() != null && asRecord.getRental().getUser() != null) {
            map.put("userId", asRecord.getRental().getUser().getId());
            map.put("userName", asRecord.getRental().getUser().getUserName());
        } else {
            map.put("userId", "-"); map.put("userName", "-");
        }
        return map;
    }

    private boolean isAsRecordMatch(AsRecord as, CommonSearchRequest request) {
        var sf = request.getSearchField();
        var sk = request.getSearchKeyword();
        if (sk == null || sk.trim().isEmpty() || "-".equals(sk)) return true; // mock 데이터 검색 무시

        boolean matchBranch = as.getBranch() != null && as.getBranch().getBranchName() != null && as.getBranch().getBranchName().contains(sk);
        boolean matchDevice = as.getDevice() != null && as.getDevice().getDeviceId() != null && as.getDevice().getDeviceId().contains(sk);
        boolean matchUser = as.getRental() != null && as.getRental().getUser() != null && as.getRental().getUser().getUserName() != null && as.getRental().getUser().getUserName().contains(sk);
        boolean matchDesc = as.getReceiptContent() != null && as.getReceiptContent().contains(sk);

        if ("branchName".equals(sf)) return matchBranch;
        if ("deviceId".equals(sf)) return matchDevice;
        if ("userName".equals(sf)) return matchUser;
        if ("asDescription".equals(sf)) return matchDesc;
        if ("all".equals(sf)) return matchBranch || matchDevice || matchUser || matchDesc;
        return true;
    }

    public CommonResponse<List<Map<String, Object>>> getAsRecords(CommonSearchRequest request) {
        List<Map<String, Object>> filteredData = new ArrayList<>();

        // [CRITICAL/HIGH] 메모리 최적화 Chunk 조회
        int chunkPage = 0;
        int chunkSize = 200;
        int maxSearchResult = 1000;

        while (true) {
            var page = asRecordRepository.findAllByIsDeletedFalse(PageRequest.of(chunkPage, chunkSize));
            if (page.isEmpty()) break;

            for (AsRecord as : page.getContent()) {
                if (isAsRecordMatch(as, request)) {
                    filteredData.add(convertToMap(as));
                }
            }
            if (filteredData.size() >= maxSearchResult) break;
            chunkPage++;
        }

        // 수동 페이지네이션 생성
        Pageable pageable = request.toPageable();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredData.size());
        List<Map<String, Object>> pagedList = start >= filteredData.size() ? List.of() : filteredData.subList(start, end);
        var pagedResult = new PageImpl<>(pagedList, pageable, filteredData.size());

        return CommonResponse.success(pagedResult.getContent(), Pagination.of(pagedResult));
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResponse<Map<String, Object>> createAsRecord(Map<String, Object> body) {
        // [HIGH] NPE 방어
        if (body.get("deviceId") == null) {
            throw new CustomException("INVALID_REQUEST", "deviceId는 필수입니다.", HttpStatus.BAD_REQUEST);
        }

        var deviceId = Long.valueOf(body.get("deviceId").toString());
        var rentalId = body.get("rentalId") != null ? Long.valueOf(body.get("rentalId").toString()) : null;
        var branchId = body.get("branchId") != null ? Long.valueOf(body.get("branchId").toString()) : null;
        var asDescription = body.get("asDescription") != null ? body.get("asDescription").toString() : "";

        var device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "기기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        var asRecord = AsRecord.builder()
                .device(device)
                .rental(rentalId != null ? rentalRepository.findById(rentalId).orElse(null) : null)
                .branch(branchId != null ? branchRepository.findById(branchId).orElse(null) : null)
                .receiptContent(asDescription)
                .status("AS_RECEIVED")
                .receiptDate(LocalDate.now())
                .isDeleted(false)
                .build();

        // [CRITICAL] A/S 접수 시 Device 상태 변경 및 DB 저장
        device.setStatus("AS_RECEIVED");
        deviceRepository.save(device);

        asRecordRepository.save(asRecord);
        return CommonResponse.created(convertToMap(asRecord));
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResponse<Map<String, Object>> updateAsRecord(Long id, Map<String, Object> body) {
        var asRecord = asRecordRepository.findById(id)
                .orElseThrow(() -> new CustomException("AS_NOT_FOUND", "A/S 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (body.containsKey("status") && body.get("status") != null) {
            String newStatus = body.get("status").toString();
            validateStatusTransition(asRecord.getStatus(), newStatus); // 상태 변경 방어
            asRecord.setStatus(newStatus);

            // 처리 완료 시 기기 상태 정상 복구
            if ("AS_COMPLETED".equals(newStatus)) {
                asRecord.setCompleteDate(LocalDate.now());
                if (asRecord.getDevice() != null) {
                    // [CRITICAL] AS 완료 후 Device 상태 변경 및 명시적 저장
                    asRecord.getDevice().setStatus("RENTAL_READY");
                    deviceRepository.save(asRecord.getDevice());
                }
            }
        }
        return CommonResponse.success(convertToMap(asRecord));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAsRecord(Long id) {
        var asRecord = asRecordRepository.findById(id)
                .orElseThrow(() -> new CustomException("AS_NOT_FOUND", "A/S 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        asRecord.setIsDeleted(true);
    }

    public CommonResponse<List<Map<String, Object>>> getUserAsRecords(Long userId, CommonSearchRequest request) {
        var page = asRecordRepository.findAllByIsDeletedFalse(request.toPageable());
        var data = page.getContent().stream()
                .filter(as -> as.getRental() != null && as.getRental().getUser() != null
                        && as.getRental().getUser().getId().equals(userId))
                .map(this::convertToMap).toList();
        return CommonResponse.success(data, Pagination.of(page));
    }

    public List<Map<String, Object>> getAsSummaryByBranch() {
        var allAsRecords = asRecordRepository.findAllByIsDeletedFalse(PageRequest.of(0, 2000)).getContent();
        return branchRepository.findAll(PageRequest.of(0, 1000)).getContent().stream()
                .filter(b -> !b.getIsDeleted())
                .map(branch -> {
                    var branchRecords = allAsRecords.stream().filter(as -> as.getBranch() != null && as.getBranch().getId().equals(branch.getId())).toList();
                    long processingCount = branchRecords.stream().filter(as -> "AS_RECEIVED".equals(as.getStatus()) || "AS_PROGRESS".equals(as.getStatus())).count();
                    return Map.<String, Object>of("branchId", branch.getId(), "branchName", branch.getBranchName(), "totalCount", branchRecords.size(), "processingCount", processingCount);
                }).toList();
    }
}