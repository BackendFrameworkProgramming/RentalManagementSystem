package hanyang.RentalManagementSystem.gyumin.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.Rental;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.BranchRepository;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.RentalRepository;
import hanyang.RentalManagementSystem.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, rollbackFor = Exception.class) // [HIGH] Checked Exception 롤백 보장
public class RentalService {

    private final RentalRepository rentalRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    // [HIGH] 상태 전이 검증 로직 적용
    private void validateStatusTransition(String current, String next) {
        var allowed = Map.of(
                "APPLIED", List.of("RECEIPT_WAITING", "RENTING", "RETURNED"),
                "RECEIPT_WAITING", List.of("RENTING"),
                "RENTING", List.of("RETURNED", "AS_RECEIVED", "REPLACED"),
                "RETURNED", List.of("RENTAL_READY")
        );
        var validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new CustomException("INVALID_STATUS_TRANSITION", current + " 상태에서 " + next + " (으)로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    private Map<String, Object> convertToMap(Rental rental) {
        var map = new HashMap<String, Object>();
        map.put("id", rental.getId());
        map.put("status", rental.getStatus());
        map.put("applyDate", rental.getApplyDate());
        map.put("returnDate", rental.getReturnDate());
        map.put("expectedStartDate", rental.getUseStartDate() != null ? rental.getUseStartDate() : "-");
        map.put("expectedReturnDate", rental.getReturnDueDate() != null ? rental.getReturnDueDate() : "-");
        map.put("receiptDate", rental.getReceiveDate() != null ? rental.getReceiveDate() : "-");
        map.put("wornStatus", rental.getWearYn() != null ? (rental.getWearYn() ? "Y" : "N") : "-");

        if (rental.getDevice() != null) {
            map.put("deviceId", rental.getDevice().getDeviceId());
            map.put("battery", rental.getDevice().getBattery() != null ? rental.getDevice().getBattery() : "-");
            var modelName = (rental.getDevice().getModelVersion() != null && rental.getDevice().getModelVersion().getModel() != null)
                    ? rental.getDevice().getModelVersion().getModel().getModelName() : "-";
            map.put("modelName", modelName);
        } else {
            map.put("deviceId", "-"); map.put("battery", "-"); map.put("modelName", "-");
        }

        if (rental.getBranch() != null) {
            map.put("branchId", rental.getBranch().getId());
            map.put("branchName", rental.getBranch().getBranchName());
        } else {
            map.put("branchId", null); map.put("branchName", "-");
        }

        if (rental.getUser() != null) {
            map.put("userId", rental.getUser().getId());
            map.put("userName", rental.getUser().getUserName());
        } else {
            map.put("userId", "-"); map.put("userName", "-");
        }
        return map;
    }

    // 필터링 분리 (가독성 향상)
    private boolean isRentalMatch(Rental r, CommonSearchRequest request) {
        var sf = request.getSearchField();
        var sk = request.getSearchKeyword();
        if (sk == null || sk.trim().isEmpty() || "-".equals(sk)) return true; // [MEDIUM] mock 데이터 검색 무시

        boolean matchBranch = r.getBranch() != null && r.getBranch().getBranchName() != null && r.getBranch().getBranchName().contains(sk);
        boolean matchDevice = r.getDevice() != null && r.getDevice().getDeviceId() != null && r.getDevice().getDeviceId().contains(sk);
        boolean matchModel = r.getDevice() != null && r.getDevice().getModelVersion() != null
                && r.getDevice().getModelVersion().getModel() != null
                && r.getDevice().getModelVersion().getModel().getModelName().contains(sk);
        boolean matchUser = r.getUser() != null && r.getUser().getUserName() != null && r.getUser().getUserName().contains(sk);
        boolean matchUserId = r.getUser() != null && r.getUser().getId().toString().equals(sk);

        if ("branchName".equals(sf)) return matchBranch;
        if ("deviceId".equals(sf)) return matchDevice;
        if ("modelName".equals(sf)) return matchModel;
        if ("userName".equals(sf)) return matchUser;
        if ("userId".equals(sf)) return matchUserId;
        if ("all".equals(sf)) return matchBranch || matchDevice || matchModel || matchUser || matchUserId;
        return true;
    }

    public CommonResponse<List<Map<String, Object>>> getRentals(CommonSearchRequest request) {
        List<Map<String, Object>> filteredData = new ArrayList<>();

        // [CRITICAL/HIGH] 메모리 OOM 방지 및 커넥션 풀 고갈을 막는 청크(Chunk) 조회 페이징
        int chunkPage = 0;
        int chunkSize = 200; // 200건씩 분할 조회
        int maxSearchResult = 1000; // 최대 검색 한도 설정

        while (true) {
            var page = rentalRepository.findAllByIsDeletedFalse(PageRequest.of(chunkPage, chunkSize));
            if (page.isEmpty()) break;

            for (Rental r : page.getContent()) {
                if (isRentalMatch(r, request)) {
                    filteredData.add(convertToMap(r));
                }
            }
            if (filteredData.size() >= maxSearchResult) break;
            chunkPage++;
        }

        // 수동 페이지네이션 계산 처리
        Pageable pageable = request.toPageable();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredData.size());
        List<Map<String, Object>> pagedList = start >= filteredData.size() ? List.of() : filteredData.subList(start, end);
        var pagedResult = new PageImpl<>(pagedList, pageable, filteredData.size());

        return CommonResponse.success(pagedResult.getContent(), Pagination.of(pagedResult));
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResponse<Map<String, Object>> createRental(Map<String, Object> body) {
        // [HIGH] NPE 방어
        if (body.get("deviceId") == null || body.get("userId") == null || body.get("branchId") == null) {
            throw new CustomException("INVALID_REQUEST", "필수 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST);
        }

        var deviceId = Long.valueOf(body.get("deviceId").toString());
        var userId = Long.valueOf(body.get("userId").toString());
        var branchId = Long.valueOf(body.get("branchId").toString());

        // [MEDIUM] 중복 임대 방지
        if (rentalRepository.existsByDeviceIdAndReturnDateIsNullAndIsDeletedFalse(deviceId)) {
            throw new CustomException("DUPLICATE_RENTAL", "해당 기기는 이미 임대 중입니다.", HttpStatus.CONFLICT);
        }

        var device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "기기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        var rental = Rental.builder()
                .device(device)
                .branch(branchRepository.findById(branchId).orElse(null))
                .user(userRepository.findById(userId).orElse(null))
                .status("RENTING")
                .applyDate(LocalDate.now())
                .isDeleted(false)
                .wearYn(false)
                .build();

        // [HIGH & CRITICAL] Device 상태 RENTING 변경 및 명시적 저장 (save 누락 방지)
        device.setStatus("RENTING");
        deviceRepository.save(device);

        rentalRepository.save(rental);
        return CommonResponse.created(convertToMap(rental));
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResponse<Map<String, Object>> updateRental(Long id, Map<String, Object> body) {
        var rental = rentalRepository.findById(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (body.containsKey("status") && body.get("status") != null) {
            String newStatus = body.get("status").toString();
            validateStatusTransition(rental.getStatus(), newStatus); // 상태 전이 방어
            rental.setStatus(newStatus);
        }

        if (body.containsKey("returnDate") && body.get("returnDate") != null) {
            try {
                // [MEDIUM] 날짜 포맷 예외 처리
                rental.setReturnDate(LocalDate.parse(body.get("returnDate").toString()));
                validateStatusTransition(rental.getStatus(), "RETURNED");
                rental.setStatus("RETURNED");

                if (rental.getDevice() != null) {
                    // [CRITICAL] 반납 시 기기 상태 RENTAL_READY 변경 후 명시적 DB 반영
                    rental.getDevice().setStatus("RENTAL_READY");
                    deviceRepository.save(rental.getDevice());
                }
            } catch (DateTimeParseException e) {
                throw new CustomException("INVALID_DATE_FORMAT", "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)", HttpStatus.BAD_REQUEST);
            }
        }

        if (body.containsKey("wearYn") && body.get("wearYn") != null) {
            rental.setWearYn(Boolean.parseBoolean(body.get("wearYn").toString()));
        }

        return CommonResponse.success(convertToMap(rental));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRental(Long id) {
        var rental = rentalRepository.findById(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        rental.setIsDeleted(true);
    }

    public CommonResponse<List<Map<String, Object>>> getUserRentals(Long userId, CommonSearchRequest request) {
        var page = rentalRepository.findAllByIsDeletedFalse(request.toPageable());
        var data = page.getContent().stream()
                .filter(r -> r.getUser() != null && r.getUser().getId().equals(userId))
                .map(this::convertToMap).toList();
        return CommonResponse.success(data, Pagination.of(page));
    }

    public List<Map<String, Object>> getRentalSummaryByBranch() {
        var allRentals = rentalRepository.findAllByIsDeletedFalse(PageRequest.of(0, 2000)).getContent(); // 청크 대신 최대량으로 타협
        return branchRepository.findAll(PageRequest.of(0, 1000)).getContent().stream()
                .filter(b -> !b.getIsDeleted())
                .map(branch -> {
                    var branchRentals = allRentals.stream().filter(r -> r.getBranch() != null && r.getBranch().getId().equals(branch.getId())).toList();
                    long rentingCount = branchRentals.stream().filter(r -> "RENTING".equals(r.getStatus())).count();
                    return Map.<String, Object>of("branchId", branch.getId(), "branchName", branch.getBranchName(), "totalCount", branchRentals.size(), "rentingCount", rentingCount);
                }).toList();
    }
}