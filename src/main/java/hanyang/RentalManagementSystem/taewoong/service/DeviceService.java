package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.ErrorInfo;
import hanyang.RentalManagementSystem.common.config.SecurityUtil;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.Branch;
import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.entity.ModelVersion;
import hanyang.RentalManagementSystem.common.enums.DeviceStatus;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.AsRecordRepository;
import hanyang.RentalManagementSystem.common.repository.BranchRepository;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.ModelVersionRepository;
import hanyang.RentalManagementSystem.common.repository.RentalRepository;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceAsHistoryResponse;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceCreateRequest;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceResponse;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceSummaryResponse;
import hanyang.RentalManagementSystem.taewoong.dto.DeviceUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ModelVersionRepository modelVersionRepository;
    private final BranchRepository branchRepository;
    private final RentalRepository rentalRepository;
    private final AsRecordRepository asRecordRepository;

    // 1-1 목록 (교수님 #1: 페이징 전에 쿼리 단계에서 isDeleted 필터 / @EntityGraph N+1 방어 / #4: DTO)
    public CommonResponse<List<DeviceResponse>> findAll(CommonSearchRequest request) {
        // 데이터 스코핑: 지점관리자는 본인 지점 디바이스만, ADMIN/STAFF는 전체
        Long scopeBranchId = SecurityUtil.isBranchManager() ? SecurityUtil.currentBranchId() : null;
        Page<Device> page;
        if (scopeBranchId != null) {
            page = deviceRepository.findAllByBranchIdAndIsDeletedFalse(scopeBranchId, request.toPageable());
        } else if (Boolean.TRUE.equals(request.getIncludeDeleted())) {
            page = deviceRepository.findAll(request.toPageable());
        } else {
            page = deviceRepository.findAllByIsDeletedFalse(request.toPageable());
        }
        List<DeviceResponse> data = page.getContent().stream().map(DeviceResponse::from).toList();
        return CommonResponse.success(data, Pagination.of(page));
    }

    // 1-2 등록
    @Transactional
    public CommonResponse<DeviceResponse> create(DeviceCreateRequest req) {
        if (req.getDeviceId() == null || req.getDeviceId().isBlank()) {
            throw new CustomException("INVALID_REQUEST", "deviceId는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (req.getModelVersionId() == null) {
            throw new CustomException("INVALID_REQUEST", "modelVersionId는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        // 디바이스ID 중복 사전 체크 (device_id는 unique 제약)
        if (deviceRepository.existsByDeviceIdAndIsDeletedFalse(req.getDeviceId())) {
            throw new CustomException("DUPLICATE_DEVICE_ID", "이미 등록된 디바이스 ID입니다.", HttpStatus.CONFLICT);
        }
        ModelVersion mv = modelVersionRepository.findById(req.getModelVersionId())
                .orElseThrow(() -> new CustomException("MODEL_VERSION_NOT_FOUND", "모델버전을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Device device = Device.builder()
                .deviceId(req.getDeviceId())
                .modelVersion(mv)
                .status(DeviceStatus.INCOMING)
                .battery(req.getBattery())
                .remark(req.getRemark())
                .incomingDate(LocalDate.now())
                .isDeleted(false)
                .build();

        if (req.getBranchId() != null) {
            Branch branch = branchRepository.findById(req.getBranchId())
                    .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
            device.setBranch(branch);
            device.setStatus(DeviceStatus.RENTAL_READY);
            device.setBranchSendDate(LocalDate.now());
        }
        // 동시성 방어: 사전 체크~저장 사이 race로 같은 ID가 먼저 들어오면 unique 제약 위반 → 깔끔한 메시지로 변환
        try {
            deviceRepository.saveAndFlush(device);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException("DUPLICATE_DEVICE_ID", "이미 등록된 디바이스 ID입니다.", HttpStatus.CONFLICT);
        }
        return CommonResponse.created(DeviceResponse.from(device));
    }

    // 1-3 상세
    public CommonResponse<DeviceResponse> findById(Long id) {
        return CommonResponse.success(DeviceResponse.from(getDevice(id)));
    }

    // 1-4 수정
    @Transactional
    public CommonResponse<DeviceResponse> update(Long id, DeviceUpdateRequest req) {
        Device device = getDevice(id);
        if (req.getStatus() != null) {
            DeviceStatus next = parseStatus(req.getStatus());
            validateStatusTransition(device.getStatus(), next);
            device.setStatus(next);
        }
        if (req.getBattery() != null) device.setBattery(req.getBattery());
        if (req.getRemark() != null) device.setRemark(req.getRemark());
        if (req.getBranchId() != null) {
            Branch branch = branchRepository.findById(req.getBranchId())
                    .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
            device.setBranch(branch);
            device.setBranchSendDate(LocalDate.now());
        }
        return CommonResponse.success(DeviceResponse.from(device));
    }

    // 1-5 삭제 (교수님 #2: 전체 렌탈 findAll 대신 exists 쿼리로 진행중 임대 확인)
    @Transactional
    public void delete(Long id) {
        Device device = getDevice(id);
        if (rentalRepository.existsByDeviceIdAndReturnDateIsNullAndIsDeletedFalse(id)) {
            throw new CustomException("DEVICE_HAS_ACTIVE_RENTAL", "진행 중인 임대가 있어 삭제할 수 없습니다.");
        }
        device.setIsDeleted(true);
    }

    // 1-5-2 상태 변경
    @Transactional
    public CommonResponse<DeviceResponse> updateStatus(Long id, String status) {
        Device d = getDevice(id);
        DeviceStatus next = parseStatus(status);
        validateStatusTransition(d.getStatus(), next);
        d.setStatus(next);
        return CommonResponse.success(DeviceResponse.from(d));
    }

    // 1-6 AS 이력 (@EntityGraph로 N+1 방어 / DTO)
    public CommonResponse<List<DeviceAsHistoryResponse>> findAsRecordsByDeviceId(Long deviceId, CommonSearchRequest request) {
        List<DeviceAsHistoryResponse> data = asRecordRepository.findAllByDeviceIdAndIsDeletedFalse(deviceId)
                .stream().map(DeviceAsHistoryResponse::from).toList();
        return CommonResponse.success(data);
    }

    // 1-7 지점 연결(단건)
    @Transactional
    public CommonResponse<DeviceResponse> linkBranch(Long deviceId, Long branchId) {
        Device device = getDevice(deviceId);
        validateStatusTransition(device.getStatus(), DeviceStatus.RENTAL_READY);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        device.setBranch(branch);
        device.setStatus(DeviceStatus.RENTAL_READY);
        device.setBranchSendDate(LocalDate.now());
        return CommonResponse.success(DeviceResponse.from(device));
    }

    // 1-8 지점 연결(다중)
    @Transactional
    public CommonResponse<Map<String, Object>> batchLinkBranch(List<Long> deviceIds, Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        List<ErrorInfo.BatchErrorDetail> errors = new ArrayList<>();
        for (Long did : deviceIds) {
            try {
                Device d = getDevice(did);
                validateStatusTransition(d.getStatus(), DeviceStatus.RENTAL_READY);
                d.setBranch(branch);
                d.setStatus(DeviceStatus.RENTAL_READY);
                d.setBranchSendDate(LocalDate.now());
            } catch (CustomException e) {
                errors.add(ErrorInfo.BatchErrorDetail.builder().targetId(did).reason(e.getCode()).build());
            }
        }
        if (!errors.isEmpty()) {
            throw new CustomException("BATCH_PARTIAL_FAILURE", errors.size() + "건 처리 실패. 전체 롤백됨.");
        }
        return CommonResponse.success(Map.of("updatedCount", deviceIds.size()));
    }

    // 1-9 지점 해제(단건)
    @Transactional
    public void unlinkBranch(Long deviceId) {
        Device device = getDevice(deviceId);
        validateStatusTransition(device.getStatus(), DeviceStatus.INCOMING);
        device.setBranch(null);
        device.setStatus(DeviceStatus.INCOMING);
        device.setBranchSendDate(null);
    }

    // 1-10 지점 해제(다중)
    @Transactional
    public CommonResponse<Map<String, Object>> batchUnlinkBranch(List<Long> deviceIds) {
        List<ErrorInfo.BatchErrorDetail> errors = new ArrayList<>();
        for (Long did : deviceIds) {
            try {
                Device d = getDevice(did);
                validateStatusTransition(d.getStatus(), DeviceStatus.INCOMING);
                d.setBranch(null);
                d.setStatus(DeviceStatus.INCOMING);
                d.setBranchSendDate(null);
            } catch (CustomException e) {
                errors.add(ErrorInfo.BatchErrorDetail.builder().targetId(did).reason(e.getCode()).build());
            }
        }
        if (!errors.isEmpty()) {
            throw new CustomException("BATCH_PARTIAL_FAILURE", errors.size() + "건 처리 실패. 전체 롤백됨.");
        }
        return CommonResponse.success(Map.of("updatedCount", deviceIds.size()));
    }

    // 1-11 집계 (교수님 #3: 전체 엔티티 로드 대신 count/group-by 쿼리)
    public CommonResponse<DeviceSummaryResponse> summaryByBranch() {
        // 데이터 스코핑: 지점관리자는 좌측 패널에 본인 지점만
        Long scopeBranchId = SecurityUtil.isBranchManager() ? SecurityUtil.currentBranchId() : null;
        List<DeviceSummaryResponse.BranchCount> branches = deviceRepository.countGroupByBranch().stream()
                .map(row -> DeviceSummaryResponse.BranchCount.builder()
                        .branchId(((Number) row[0]).longValue())
                        .branchName((String) row[1])
                        .count(((Number) row[2]).longValue())
                        .build())
                .filter(bc -> scopeBranchId == null || scopeBranchId.equals(bc.getBranchId()))
                .toList();
        DeviceSummaryResponse summary = DeviceSummaryResponse.builder()
                .branches(branches)
                .total(deviceRepository.countByIsDeletedFalse())
                .unshipped(deviceRepository.countByBranchIsNullAndIsDeletedFalse())
                .shipped(deviceRepository.countByStatusInAndIsDeletedFalse(List.of(DeviceStatus.RENTING, DeviceStatus.RENTAL_READY)))
                .returned(deviceRepository.countByStatusAndIsDeletedFalse(DeviceStatus.RETURNED))
                .disposed(deviceRepository.countByStatusAndIsDeletedFalse(DeviceStatus.DISPOSED))
                .build();
        return CommonResponse.success(summary);
    }

    // 모델버전별 디바이스 수 (교수님 #3: count 쿼리)
    public long countByModelVersionId(Long modelVersionId) {
        return deviceRepository.countByModelVersionIdAndIsDeletedFalse(modelVersionId);
    }

    // === 헬퍼 ===
    private Device getDevice(Long id) {
        return deviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND",
                        "ID " + id + "에 해당하는 디바이스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private DeviceStatus parseStatus(String s) {
        try {
            return DeviceStatus.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            throw new CustomException("INVALID_STATUS", "알 수 없는 상태값: " + s, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateStatusTransition(DeviceStatus current, DeviceStatus next) {
        Map<DeviceStatus, List<DeviceStatus>> allowed = Map.of(
                DeviceStatus.INCOMING, List.of(DeviceStatus.RENTAL_READY),
                DeviceStatus.RENTAL_READY, List.of(DeviceStatus.RENTING, DeviceStatus.AS_RECEIVED, DeviceStatus.INCOMING),
                DeviceStatus.RENTING, List.of(DeviceStatus.RENTAL_READY, DeviceStatus.AS_RECEIVED),
                DeviceStatus.AS_RECEIVED, List.of(DeviceStatus.AS_PROGRESS, DeviceStatus.RENTAL_READY, DeviceStatus.DISPOSED),
                DeviceStatus.AS_PROGRESS, List.of(DeviceStatus.RENTAL_READY, DeviceStatus.DISPOSED),
                DeviceStatus.RETURNED, List.of(DeviceStatus.INCOMING),
                DeviceStatus.DISPOSED, List.of()
        );
        List<DeviceStatus> validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new CustomException("INVALID_STATUS_TRANSITION", current + " → " + next + " 전이는 허용되지 않습니다.");
        }
    }
}
