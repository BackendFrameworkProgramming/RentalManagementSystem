package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.common.entity.*;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ModelVersionRepository modelVersionRepository;
    private final BranchRepository branchRepository;
    private final RentalRepository rentalRepository;
    private final AsRecordRepository asRecordRepository;

    // 1-1 목록 조회
    public CommonResponse<List<Map<String, Object>>> findAll(CommonSearchRequest request) {
        Page<Device> page = deviceRepository.findAll(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream()
                .filter(d -> request.getIncludeDeleted() || !d.getIsDeleted())
                .map(this::toMap)
                .collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    // 1-2 등록
    @Transactional
    public CommonResponse<Map<String, Object>> create(Map<String, Object> body) {
        String deviceId = (String) body.get("deviceId");
        Long modelVersionId = ((Number) body.get("modelVersionId")).longValue();

        ModelVersion mv = modelVersionRepository.findById(modelVersionId)
                .orElseThrow(() -> new CustomException("MODEL_VERSION_NOT_FOUND", "모델버전을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Device device = Device.builder()
                .deviceId(deviceId)
                .modelVersion(mv)
                .status("INCOMING")
                .battery((String) body.get("battery"))
                .incomingDate(LocalDate.now())
                .isDeleted(false)
                .build();

        if (body.get("branchId") != null) {
            Long branchId = ((Number) body.get("branchId")).longValue();
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
            device.setBranch(branch);
            device.setStatus("RENTAL_READY");
        }

        deviceRepository.save(device);
        return CommonResponse.created(toMap(device));
    }

    // 1-3 상세
    public CommonResponse<Map<String, Object>> findById(Long id) {
        Device device = getDevice(id);
        return CommonResponse.success(toMap(device));
    }

    // 1-4 수정
    @Transactional
    public CommonResponse<Map<String, Object>> update(Long id, Map<String, Object> body) {
        Device device = getDevice(id);

        if (body.containsKey("status")) {
            String newStatus = (String) body.get("status");
            validateStatusTransition(device.getStatus(), newStatus);
            device.setStatus(newStatus);
        }
        if (body.containsKey("battery")) device.setBattery((String) body.get("battery"));
        if (body.containsKey("remark")) device.setRemark((String) body.get("remark"));

        return CommonResponse.success(toMap(device));
    }

    // 1-5 삭제
    @Transactional
    public void delete(Long id) {
        Device device = getDevice(id);
        // 진행 중 임대 확인
        boolean hasActiveRental = rentalRepository.findAll().stream()
                .anyMatch(r -> r.getDevice() != null && r.getDevice().getId().equals(id)
                        && !r.getIsDeleted() && r.getReturnDate() == null);
        if (hasActiveRental) {
            throw new CustomException("DEVICE_HAS_ACTIVE_RENTAL", "진행 중인 임대가 있어 삭제할 수 없습니다.");
        }
        device.setIsDeleted(true);
    }

    @Transactional
    public CommonResponse<Map<String, Object>> updateStatus(Long id, String newStatus) {
        Device d = deviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "디바이스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        validateStatusTransition(d.getStatus(), newStatus);
        d.setStatus(newStatus);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("deviceId", d.getDeviceId());
        m.put("status", d.getStatus());
        return CommonResponse.success(m);
    }

    // 1-6 AS 이력
    public CommonResponse<List<Map<String, Object>>> findAsRecordsByDeviceId(Long deviceId, CommonSearchRequest request) {
        Page<AsRecord> page = asRecordRepository.findAll(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream()
                .filter(r -> r.getDevice().getId().equals(deviceId) && !r.getIsDeleted())
                .map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", r.getId());
                    map.put("status", r.getStatus());
                    map.put("receiptDate", r.getReceiptDate());
                    map.put("receiptBy", r.getReceiptBy());
                    map.put("receiptContent", r.getReceiptContent());
                    map.put("confirmResult", r.getConfirmResult());
                    map.put("repairContent", r.getRepairContent());
                    map.put("completeDate", r.getCompleteDate());
                    if (r.getRental() != null) {
                        map.put("userName", r.getRental().getUser().getUserName());
                        map.put("rentalDate", r.getRental().getApplyDate());
                    }
                    return map;
                })
                .collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    // 1-7 지점 연결 (단건)
    @Transactional
    public CommonResponse<Map<String, Object>> linkBranch(Long deviceId, Long branchId) {
        Device device = getDevice(deviceId);
        validateStatusTransition(device.getStatus(), "RENTAL_READY");
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        device.setBranch(branch);
        device.setStatus("RENTAL_READY");
        device.setBranchSendDate(LocalDate.now());
        return CommonResponse.success(toMap(device));
    }

    // 1-8 지점 연결 (다중)
    @Transactional
    public CommonResponse<Map<String, Object>> batchLinkBranch(List<Long> deviceIds, Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        List<ErrorInfo.BatchErrorDetail> errors = new ArrayList<>();
        for (Long did : deviceIds) {
            try {
                Device d = getDevice(did);
                validateStatusTransition(d.getStatus(), "RENTAL_READY");
                d.setBranch(branch);
                d.setStatus("RENTAL_READY");
                d.setBranchSendDate(LocalDate.now());
            } catch (CustomException e) {
                errors.add(ErrorInfo.BatchErrorDetail.builder().targetId(did).reason(e.getCode()).build());
            }
        }
        if (!errors.isEmpty()) {
            throw new CustomException("BATCH_PARTIAL_FAILURE",
                    errors.size() + "건 처리 실패. 전체 롤백됨.");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("updatedCount", deviceIds.size());
        return CommonResponse.success(result);
    }

    // 1-9 지점 해제 (단건)
    @Transactional
    public void unlinkBranch(Long deviceId) {
        Device device = getDevice(deviceId);
        validateStatusTransition(device.getStatus(), "INCOMING");
        device.setBranch(null);
        device.setStatus("INCOMING");
    }

    // 1-10 지점 해제 (다중)
    @Transactional
    public CommonResponse<Map<String, Object>> batchUnlinkBranch(List<Long> deviceIds) {
        List<ErrorInfo.BatchErrorDetail> errors = new ArrayList<>();
        for (Long did : deviceIds) {
            try {
                Device d = getDevice(did);
                validateStatusTransition(d.getStatus(), "INCOMING");
                d.setBranch(null);
                d.setStatus("INCOMING");
            } catch (CustomException e) {
                errors.add(ErrorInfo.BatchErrorDetail.builder().targetId(did).reason(e.getCode()).build());
            }
        }
        if (!errors.isEmpty()) {
            throw new CustomException("BATCH_PARTIAL_FAILURE",
                    errors.size() + "건 처리 실패. 전체 롤백됨.");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("updatedCount", deviceIds.size());
        return CommonResponse.success(result);
    }

    // 1-11 지점별 수량 집계
    public List<Map<String, Object>> summaryByBranch() {
        return deviceRepository.findAll().stream()
                .filter(d -> !d.getIsDeleted() && d.getBranch() != null)
                .collect(Collectors.groupingBy(d -> d.getBranch().getId()))
                .entrySet().stream()
                .map(entry -> {
                    Device sample = entry.getValue().get(0);
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("branchId", entry.getKey());
                    map.put("branchName", sample.getBranch().getBranchName());
                    map.put("count", entry.getValue().size());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // === 헬퍼 ===
    private Device getDevice(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND",
                        "ID " + id + "에 해당하는 디바이스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private Map<String, Object> toMap(Device d) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", d.getId());
        map.put("deviceId", d.getDeviceId());
        map.put("status", d.getStatus());
        map.put("battery", d.getBattery());
        map.put("branchId", d.getBranch() != null ? d.getBranch().getId() : null);
        map.put("branchName", d.getBranch() != null ? d.getBranch().getBranchName() : null);
        map.put("modelVersionId", d.getModelVersion().getId());
        map.put("modelName", d.getModelVersion().getModel().getModelName());
        map.put("version", d.getModelVersion().getVersion());
        map.put("incomingDate", d.getIncomingDate());
        map.put("latestRentalDate", d.getLatestRentalDate());
        map.put("latestAsDate", d.getLatestAsDate());
        map.put("remark", d.getRemark());
        map.put("createdAt", d.getCreatedAt());
        map.put("updatedAt", d.getUpdatedAt());
        return map;
    }

    private void validateStatusTransition(String current, String next) {
        Map<String, List<String>> allowed = Map.of(
                "INCOMING", List.of("RENTAL_READY"),
                "RENTAL_READY", List.of("RENTING", "AS_RECEIVED", "INCOMING"),
                "RENTING", List.of("RENTAL_READY", "AS_RECEIVED"),
                "AS_RECEIVED", List.of("AS_PROGRESS", "RENTAL_READY", "DISPOSED"),
                "AS_PROGRESS", List.of("RENTAL_READY", "DISPOSED"),
                "RETURNED", List.of("INCOMING"),
                "DISPOSED", List.of()
        );
        List<String> validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new CustomException("INVALID_STATUS_TRANSITION",
                    current + " → " + next + " 전이는 허용되지 않습니다.");
        }
    }
}
