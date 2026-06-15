package hanyang.RentalManagementSystem.gyumin.service;

import hanyang.RentalManagementSystem.common.config.SecurityUtil;
import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.entity.Rental;
import hanyang.RentalManagementSystem.common.enums.DeviceStatus;
import hanyang.RentalManagementSystem.common.enums.RentalStatus;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.BranchRepository;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.RentalRepository;
import hanyang.RentalManagementSystem.common.repository.UserRepository;
import hanyang.RentalManagementSystem.gyumin.dto.RentalBranchSummaryResponse;
import hanyang.RentalManagementSystem.gyumin.dto.RentalCreateRequest;
import hanyang.RentalManagementSystem.gyumin.dto.RentalResponse;
import hanyang.RentalManagementSystem.gyumin.dto.RentalUpdateRequest;
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
public class RentalService {

    private final RentalRepository rentalRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public CommonResponse<List<RentalResponse>> getRentals(CommonSearchRequest request) {
        Long targetBranchId = SecurityUtil.isAdmin() ? null : SecurityUtil.currentBranchId();
        String keyword = normalizeKeyword(request.getSearchKeyword());
        Pageable pageable = request.toPageable();

        Page<Rental> rentalPage = rentalRepository.searchRentals(targetBranchId, null, keyword, pageable);

        Pagination pagination = new Pagination();
        pagination.setPage(request.getPage());
        pagination.setSize(request.getSize());
        pagination.setTotalElements(rentalPage.getTotalElements());
        pagination.setTotalPages(rentalPage.getTotalPages());

        List<RentalResponse> responses = rentalPage.stream().map(RentalResponse::from).toList();
        return CommonResponse.success(responses, pagination);
    }

    @Transactional
    public CommonResponse<RentalResponse> createRental(RentalCreateRequest request) {
        Device device = deviceRepository.findByIdAndIsDeletedFalse(request.getDeviceId())
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "디바이스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (device.getStatus() != DeviceStatus.RENTAL_READY) {
            throw new CustomException("DEVICE_NOT_AVAILABLE", "임대 가능한 상태가 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Rental rental = Rental.builder()
                .user(userRepository.getReferenceById(request.getUserId()))
                .device(device)
                .branch(branchRepository.getReferenceById(request.getBranchId()))
                .status(RentalStatus.APPLIED)
                .applyDate(LocalDate.now())
                .build();

        device.setStatus(DeviceStatus.RENTING);
        rentalRepository.save(rental);

        return CommonResponse.created(RentalResponse.from(rental));
    }

    @Transactional
    public CommonResponse<RentalResponse> updateRental(Long id, RentalUpdateRequest request) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        RentalStatus nextStatus = parseStatus(request.getStatus());
        validateStatusTransition(rental.getStatus(), nextStatus);

        rental.setStatus(nextStatus);

        if (nextStatus == RentalStatus.RETURNED || nextStatus == RentalStatus.REPLACED) {
            rental.setReturnDate(LocalDate.now());
            rental.getDevice().setStatus(DeviceStatus.RENTAL_READY);
        } else if (nextStatus == RentalStatus.AS_RECEIVED) {
            rental.getDevice().setStatus(DeviceStatus.AS_RECEIVED);
        }

        return CommonResponse.success(RentalResponse.from(rental));
    }

    @Transactional
    public CommonResponse<Void> deleteRental(Long id) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (rental.getStatus() != RentalStatus.APPLIED && rental.getStatus() != RentalStatus.RETURNED) {
            throw new CustomException("DELETE_NOT_ALLOWED", "삭제할 수 없는 상태입니다.", HttpStatus.BAD_REQUEST);
        }

        rental.getDevice().setStatus(DeviceStatus.RENTAL_READY);
        rentalRepository.delete(rental);
        return CommonResponse.success(null);
    }

    @Transactional(readOnly = true)
    public CommonResponse<List<RentalResponse>> getUserRentals(Long userId, CommonSearchRequest request) {
        String keyword = normalizeKeyword(request.getSearchKeyword());
        Pageable pageable = request.toPageable();

        Page<Rental> rentalPage = rentalRepository.searchRentals(null, userId, keyword, pageable);

        Pagination pagination = new Pagination();
        pagination.setPage(request.getPage());
        pagination.setSize(request.getSize());
        pagination.setTotalElements(rentalPage.getTotalElements());
        pagination.setTotalPages(rentalPage.getTotalPages());

        List<RentalResponse> responses = rentalPage.stream().map(RentalResponse::from).toList();
        return CommonResponse.success(responses, pagination);
    }

    @Transactional(readOnly = true)
    public List<RentalBranchSummaryResponse> getRentalSummaryByBranch() {
        // 데이터 스코핑: 지점관리자는 좌측 패널에 본인 지점만
        Long scopeBranchId = SecurityUtil.isBranchManager() ? SecurityUtil.currentBranchId() : null;
        return rentalRepository.summaryByBranch(RentalStatus.RENTING).stream()
                .map(row -> RentalBranchSummaryResponse.builder()
                        .branchId(((Number) row[0]).longValue())
                        .branchName((String) row[1])
                        .totalCount(((Number) row[2]).longValue())
                        .rentingCount(((Number) row[3]).longValue())
                        .build())
                .filter(r -> scopeBranchId == null || scopeBranchId.equals(r.getBranchId()))
                .toList();
    }

    // 💡 부활시킨 메서드
    private String normalizeKeyword(String kw) {
        return (kw == null || kw.trim().isEmpty() || "-".equals(kw)) ? null : kw.trim();
    }

    private RentalStatus parseStatus(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return RentalStatus.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            throw new CustomException("INVALID_STATUS", "알 수 없는 상태값: " + s, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateStatusTransition(RentalStatus current, RentalStatus next) {
        Map<RentalStatus, List<RentalStatus>> allowed = Map.of(
                RentalStatus.APPLIED, List.of(RentalStatus.RECEIPT_WAITING, RentalStatus.RENTING, RentalStatus.RETURNED),
                RentalStatus.RECEIPT_WAITING, List.of(RentalStatus.RENTING),
                RentalStatus.RENTING, List.of(RentalStatus.RETURNED, RentalStatus.AS_RECEIVED, RentalStatus.REPLACED),
                RentalStatus.RETURNED, List.of(RentalStatus.RENTAL_READY)
        );
        List<RentalStatus> validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new CustomException("INVALID_STATUS_TRANSITION", "상태 변경 불가", HttpStatus.BAD_REQUEST);
        }
    }
}