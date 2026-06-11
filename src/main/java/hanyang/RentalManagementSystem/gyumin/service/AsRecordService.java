package hanyang.RentalManagementSystem.gyumin.service;

import hanyang.RentalManagementSystem.common.config.SecurityUtil;
import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.AsRecord;
import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.enums.DeviceStatus;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.AsRecordRepository;
import hanyang.RentalManagementSystem.common.repository.BranchRepository;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.RentalRepository;
import hanyang.RentalManagementSystem.gyumin.dto.AsBranchSummaryResponse;
import hanyang.RentalManagementSystem.gyumin.dto.AsRecordCreateRequest;
import hanyang.RentalManagementSystem.gyumin.dto.AsRecordResponse;
import hanyang.RentalManagementSystem.gyumin.dto.AsRecordUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class AsRecordService {

    private final AsRecordRepository asRecordRepository;
    private final DeviceRepository deviceRepository;
    private final RentalRepository rentalRepository;
    private final BranchRepository branchRepository;

    // A/S 목록 (교수님 #1: 청크 스캔 제거 → 쿼리 필터/페이징, #4: DTO)
    // 데이터 스코핑: 일반 USER=본인(rental.user) AS만, BRANCH_MANAGER=본인 지점만, ADMIN/STAFF=전체
    public CommonResponse<List<AsRecordResponse>> getAsRecords(CommonSearchRequest request) {
        Long branchId = null;
        Long userId = null;
        if (SecurityUtil.isBranchManager()) {
            branchId = SecurityUtil.currentBranchId();
        } else if (SecurityUtil.isUser()) {
            userId = SecurityUtil.currentUserId();
        }
        String kw = normalizeKeyword(request.getSearchKeyword());
        Page<AsRecord> page = asRecordRepository.searchAsRecords(branchId, userId, kw, request.toPageable());
        return CommonResponse.success(page.getContent().stream().map(AsRecordResponse::from).toList(), Pagination.of(page));
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResponse<AsRecordResponse> createAsRecord(AsRecordCreateRequest req) {
        if (req.getDeviceId() == null) {
            throw new CustomException("INVALID_REQUEST", "deviceId는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        Device device = deviceRepository.findById(req.getDeviceId())
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "기기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        AsRecord asRecord = AsRecord.builder()
                .device(device)
                .rental(req.getRentalId() != null ? rentalRepository.findById(req.getRentalId()).orElse(null) : null)
                .branch(req.getBranchId() != null ? branchRepository.findById(req.getBranchId()).orElse(null) : null)
                .receiptContent(req.getAsDescription() != null ? req.getAsDescription() : "")
                .status("AS_RECEIVED")
                .receiptDate(LocalDate.now())
                .isDeleted(false)
                .build();

        device.setStatus(DeviceStatus.AS_RECEIVED);
        deviceRepository.save(device);
        asRecordRepository.save(asRecord);
        return CommonResponse.created(AsRecordResponse.from(asRecord));
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResponse<AsRecordResponse> updateAsRecord(Long id, AsRecordUpdateRequest req) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_NOT_FOUND", "A/S 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (req.getStatus() != null) {
            String newStatus = req.getStatus();
            validateStatusTransition(asRecord.getStatus(), newStatus);
            asRecord.setStatus(newStatus);

            if ("AS_COMPLETED".equals(newStatus)) {
                asRecord.setCompleteDate(LocalDate.now());
                if (asRecord.getDevice() != null) {
                    asRecord.getDevice().setStatus(DeviceStatus.RENTAL_READY);
                    deviceRepository.save(asRecord.getDevice());
                }
            }
        }
        return CommonResponse.success(AsRecordResponse.from(asRecord));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAsRecord(Long id) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_NOT_FOUND", "A/S 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        asRecord.setIsDeleted(true);
    }

    // 특정 유저의 A/S 이력 (IDOR 방어: 본인 또는 ADMIN/STAFF만)
    public CommonResponse<List<AsRecordResponse>> getUserAsRecords(Long userId, CommonSearchRequest request) {
        if (!SecurityUtil.canSeeAll() && !userId.equals(SecurityUtil.currentUserId())) {
            throw new CustomException("FORBIDDEN", "본인 A/S 이력만 조회할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
        Page<AsRecord> page = asRecordRepository.searchAsRecords(null, userId, null, request.toPageable());
        return CommonResponse.success(page.getContent().stream().map(AsRecordResponse::from).toList(), Pagination.of(page));
    }

    // 지점별 요약 (교수님 #3: group by 쿼리)
    public List<AsBranchSummaryResponse> getAsSummaryByBranch() {
        return asRecordRepository.summaryByBranch().stream()
                .map(row -> AsBranchSummaryResponse.builder()
                        .branchId(((Number) row[0]).longValue())
                        .branchName((String) row[1])
                        .totalCount(((Number) row[2]).longValue())
                        .processingCount(((Number) row[3]).longValue())
                        .build())
                .toList();
    }

    private String normalizeKeyword(String kw) {
        return (kw == null || kw.trim().isEmpty() || "-".equals(kw)) ? null : kw.trim();
    }

    // TODO(gyumin): 교수님 피드백 #5 — 아래 String status를 AsStatus enum으로 전환.
    //   참고: gyumin/service/RentalService.validateStatusTransition(RentalStatus) 방식 그대로.
    private void validateStatusTransition(String current, String next) {
        Map<String, List<String>> allowed = Map.of(
                "AS_RECEIVED", List.of("AS_PROGRESS", "AS_COMPLETED"),
                "AS_PROGRESS", List.of("AS_COMPLETED")
        );
        List<String> validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new CustomException("INVALID_STATUS_TRANSITION", current + " 에서 " + next + "(으)로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
    }
}
