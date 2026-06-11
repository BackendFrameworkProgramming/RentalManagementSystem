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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, rollbackFor = Exception.class) // checked exception 롤백 보장
public class RentalService {

    private final RentalRepository rentalRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    // 임대 목록 (교수님 #1: 청크 전체스캔+메모리필터 제거 → 쿼리 단계 필터/페이징, @EntityGraph N+1 방어)
    // 데이터 스코핑(OWASP A01/IDOR): 일반 USER=본인 임대만, BRANCH_MANAGER=본인 지점만, ADMIN/STAFF=전체
    public CommonResponse<List<RentalResponse>> getRentals(CommonSearchRequest request) {
        Long branchId = null;
        Long userId = null;
        if (SecurityUtil.isBranchManager()) {
            branchId = SecurityUtil.currentBranchId();
        } else if (SecurityUtil.isUser()) {
            userId = SecurityUtil.currentUserId();
        }
        String kw = normalizeKeyword(request.getSearchKeyword());
        Page<Rental> page = rentalRepository.searchRentals(branchId, userId, kw, request.toPageable());
        return CommonResponse.success(page.getContent().stream().map(RentalResponse::from).toList(), Pagination.of(page));
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResponse<RentalResponse> createRental(RentalCreateRequest req) {
        if (req.getDeviceId() == null || req.getUserId() == null || req.getBranchId() == null) {
            throw new CustomException("INVALID_REQUEST", "필수 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST);
        }
        if (rentalRepository.existsByDeviceIdAndReturnDateIsNullAndIsDeletedFalse(req.getDeviceId())) {
            throw new CustomException("DUPLICATE_RENTAL", "해당 기기는 이미 임대 중입니다.", HttpStatus.CONFLICT);
        }
        Device device = deviceRepository.findById(req.getDeviceId())
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "기기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Rental rental = Rental.builder()
                .device(device)
                .branch(branchRepository.findById(req.getBranchId()).orElse(null))
                .user(userRepository.findById(req.getUserId()).orElse(null))
                .status(RentalStatus.RENTING)
                .applyDate(LocalDate.now())
                .isDeleted(false)
                .wearYn(false)
                .build();

        device.setStatus(DeviceStatus.RENTING);
        deviceRepository.save(device);
        rentalRepository.save(rental);
        return CommonResponse.created(RentalResponse.from(rental));
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResponse<RentalResponse> updateRental(Long id, RentalUpdateRequest req) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (req.getStatus() != null) {
            RentalStatus next = parseStatus(req.getStatus());
            validateStatusTransition(rental.getStatus(), next);
            rental.setStatus(next);
        }

        if (req.getReturnDate() != null) {
            try {
                rental.setReturnDate(LocalDate.parse(req.getReturnDate()));
                validateStatusTransition(rental.getStatus(), RentalStatus.RETURNED);
                rental.setStatus(RentalStatus.RETURNED);
                if (rental.getDevice() != null) {
                    rental.getDevice().setStatus(DeviceStatus.RENTAL_READY);
                    deviceRepository.save(rental.getDevice());
                }
            } catch (DateTimeParseException e) {
                throw new CustomException("INVALID_DATE_FORMAT", "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)", HttpStatus.BAD_REQUEST);
            }
        }

        if (req.getWearYn() != null) {
            rental.setWearYn(req.getWearYn());
        }
        return CommonResponse.success(RentalResponse.from(rental));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRental(Long id) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        rental.setIsDeleted(true);
    }

    // 특정 유저의 임대 이력 (IDOR 방어: 본인 또는 ADMIN/STAFF만)
    public CommonResponse<List<RentalResponse>> getUserRentals(Long userId, CommonSearchRequest request) {
        if (!SecurityUtil.canSeeAll() && !userId.equals(SecurityUtil.currentUserId())) {
            throw new CustomException("FORBIDDEN", "본인 임대 이력만 조회할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
        Page<Rental> page = rentalRepository.findAllByUserIdAndIsDeletedFalse(userId, request.toPageable());
        return CommonResponse.success(page.getContent().stream().map(RentalResponse::from).toList(), Pagination.of(page));
    }

    // 지점별 요약 (교수님 #3: 전체 로드 대신 group by 쿼리)
    public List<RentalBranchSummaryResponse> getRentalSummaryByBranch() {
        return rentalRepository.summaryByBranch(RentalStatus.RENTING).stream()
                .map(row -> RentalBranchSummaryResponse.builder()
                        .branchId(((Number) row[0]).longValue())
                        .branchName((String) row[1])
                        .totalCount(((Number) row[2]).longValue())
                        .rentingCount(((Number) row[3]).longValue())
                        .build())
                .toList();
    }

    private String normalizeKeyword(String kw) {
        return (kw == null || kw.trim().isEmpty() || "-".equals(kw)) ? null : kw.trim();
    }

    private RentalStatus parseStatus(String s) {
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
            throw new CustomException("INVALID_STATUS_TRANSITION",
                    current + " 상태에서 " + next + " (으)로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
    }
}
