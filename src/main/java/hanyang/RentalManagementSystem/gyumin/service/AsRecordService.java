package hanyang.RentalManagementSystem.gyumin.service;

import hanyang.RentalManagementSystem.common.config.SecurityUtil;
import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.AsRecord;
import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.enums.AsStatus;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AsRecordService {

    private final AsRecordRepository asRecordRepository;
    private final DeviceRepository deviceRepository;
    private final RentalRepository rentalRepository;
    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public CommonResponse<List<AsRecordResponse>> getAsRecords(CommonSearchRequest request) {
        Long targetBranchId = SecurityUtil.isAdmin() ? null : SecurityUtil.currentBranchId();
        String keyword = normalizeKeyword(request.getSearchKeyword());
        Pageable pageable = request.toPageable();

        Page<AsRecord> recordPage = asRecordRepository.searchAsRecords(targetBranchId, null, keyword, pageable);

        Pagination pagination = new Pagination();
        pagination.setPage(request.getPage());
        pagination.setSize(request.getSize());
        pagination.setTotalElements(recordPage.getTotalElements());
        pagination.setTotalPages(recordPage.getTotalPages());

        List<AsRecordResponse> responses = recordPage.stream().map(AsRecordResponse::from).toList();
        return CommonResponse.success(responses, pagination);
    }

    // 💡 상세 모달창용 서비스 로직 추가
    @Transactional(readOnly = true)
    public CommonResponse<AsRecordResponse> getAsRecord(Long id) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "A/S 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return CommonResponse.success(AsRecordResponse.from(asRecord));
    }

    @Transactional
    public CommonResponse<AsRecordResponse> createAsRecord(AsRecordCreateRequest request) {
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "디바이스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        AsRecord asRecord = AsRecord.builder()
                .device(device)
                .rental(request.getRentalId() != null ? rentalRepository.getReferenceById(request.getRentalId()) : null)
                .branch(branchRepository.getReferenceById(request.getBranchId()))
                .receiptDate(LocalDate.now())
                .receiptContent(request.getAsDescription())
                .build();

        asRecord.setStatusEnum(AsStatus.AS_RECEIVED);
        device.setStatus(DeviceStatus.AS_RECEIVED);
        asRecordRepository.save(asRecord);

        return CommonResponse.created(AsRecordResponse.from(asRecord));
    }

    @Transactional
    public CommonResponse<AsRecordResponse> updateAsRecord(Long id, AsRecordUpdateRequest request) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "A/S 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        AsStatus nextStatus = AsStatus.fromString(request.getStatus());
        validateStatusTransition(asRecord.getStatusEnum(), nextStatus);

        asRecord.setStatusEnum(nextStatus);

        if (nextStatus == AsStatus.AS_COMPLETED) {
            asRecord.setCompleteDate(LocalDate.now());
            asRecord.getDevice().setStatus(DeviceStatus.RENTAL_READY);
        } else if (nextStatus == AsStatus.AS_PROGRESS) {
            asRecord.setConfirmDate(LocalDate.now());
        }

        return CommonResponse.success(AsRecordResponse.from(asRecord));
    }

    @Transactional
    public CommonResponse<Void> deleteAsRecord(Long id) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "A/S 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (asRecord.getStatusEnum() != AsStatus.AS_RECEIVED) {
            throw new CustomException("DELETE_NOT_ALLOWED", "접수 상태에서만 삭제할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        asRecord.getDevice().setStatus(DeviceStatus.RENTAL_READY);
        asRecordRepository.delete(asRecord);
        return CommonResponse.success(null);
    }

    @Transactional(readOnly = true)
    public CommonResponse<List<AsRecordResponse>> getUserAsRecords(Long userId, CommonSearchRequest request) {
        String keyword = normalizeKeyword(request.getSearchKeyword());
        Pageable pageable = request.toPageable();

        Page<AsRecord> recordPage = asRecordRepository.searchAsRecords(null, userId, keyword, pageable);

        Pagination pagination = new Pagination();
        pagination.setPage(request.getPage());
        pagination.setSize(request.getSize());
        pagination.setTotalElements(recordPage.getTotalElements());
        pagination.setTotalPages(recordPage.getTotalPages());

        List<AsRecordResponse> responses = recordPage.stream().map(AsRecordResponse::from).toList();
        return CommonResponse.success(responses, pagination);
    }

    public CommonResponse<List<AsBranchSummaryResponse>> getAsSummaryByBranch() {
        // 데이터 스코핑: 지점관리자는 좌측 패널에 본인 지점만
        Long scopeBranchId = SecurityUtil.isBranchManager() ? SecurityUtil.currentBranchId() : null;
        List<AsBranchSummaryResponse> list = asRecordRepository.summaryByBranch().stream()
                .map(row -> AsBranchSummaryResponse.builder()
                        .branchId(((Number) row[0]).longValue())
                        .branchName((String) row[1])
                        .totalCount(((Number) row[2]).longValue())
                        .processingCount(((Number) row[3]).longValue())
                        .build())
                .filter(r -> scopeBranchId == null || scopeBranchId.equals(r.getBranchId()))
                .toList();
        return CommonResponse.success(list);
    }

    private String normalizeKeyword(String kw) {
        return (kw == null || kw.trim().isEmpty() || "-".equals(kw)) ? null : kw.trim();
    }

    private void validateStatusTransition(AsStatus current, AsStatus next) {
        Map<AsStatus, List<AsStatus>> allowed = Map.of(
                AsStatus.AS_RECEIVED, List.of(AsStatus.AS_PROGRESS, AsStatus.AS_COMPLETED),
                AsStatus.AS_PROGRESS, List.of(AsStatus.AS_COMPLETED)
        );
        List<AsStatus> validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new CustomException("INVALID_STATUS", "잘못된 상태 변경입니다.", HttpStatus.BAD_REQUEST);
        }
    }
}