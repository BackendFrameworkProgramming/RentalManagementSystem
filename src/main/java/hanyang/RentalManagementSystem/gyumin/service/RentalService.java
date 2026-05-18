package hanyang.RentalManagementSystem.gyumin.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.entity.Rental;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.BranchRepository;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.RentalRepository;
import hanyang.RentalManagementSystem.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalService {

    private final RentalRepository rentalRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    // 기획서의 상태 매핑: 신청(APPLIED), 사용중(RENTING), 반납(RETURNED) 등을 포함하도록 확장
    private void validateStatusTransition(String current, String next) {
        Map<String, List<String>> allowed = Map.of(
                "APPLIED", List.of("RECEIPT_WAITING", "RENTING"),
                "RECEIPT_WAITING", List.of("RENTING"),
                "RENTAL_READY", List.of("RENTING", "AS_RECEIVED", "INCOMING"),
                "RENTING", List.of("RENTAL_READY", "AS_RECEIVED", "RETURNED", "REPLACED"),
                "AS_RECEIVED", List.of("AS_PROGRESS", "RENTAL_READY", "DISPOSED"),
                "RETURNED", List.of("INCOMING", "RENTAL_READY"),
                "REPLACED", List.of("RETURNED")
        );
        List<String> validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new CustomException("INVALID_STATUS_TRANSITION", current + " → " + next + " 전이는 허용되지 않습니다.");
        }
    }

    // 기획서 15개 컬럼 매핑 (공통 엔티티에 없는 필드는 대시보드 표현을 위해 빈 값 처리)
    private Map<String, Object> convertToMap(Rental rental) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", rental.getId());
        map.put("status", rental.getStatus());
        map.put("applyDate", rental.getApplyDate());
        map.put("returnDate", rental.getReturnDate());

        // 엔티티에 없는 기획서 요구 필드 가라(Mock) 데이터 매핑
        map.put("expectedStartDate", "-");
        map.put("expectedReturnDate", "-");
        map.put("receiptDate", "-");
        map.put("wornStatus", "-");

        if (rental.getDevice() != null) {
            map.put("deviceId", rental.getDevice().getDeviceId());
            map.put("battery", rental.getDevice().getBattery());
            if (rental.getDevice().getModelVersion() != null && rental.getDevice().getModelVersion().getModel() != null) {
                map.put("modelName", rental.getDevice().getModelVersion().getModel().getModelName());
            } else {
                map.put("modelName", "-");
            }
        } else {
            map.put("deviceId", "-");
            map.put("battery", "-");
            map.put("modelName", "-");
        }

        if (rental.getBranch() != null) {
            map.put("branchId", rental.getBranch().getId());
            map.put("branchName", rental.getBranch().getBranchName());
        } else {
            map.put("branchId", null);
            map.put("branchName", "-");
        }

        if (rental.getUser() != null) {
            map.put("userId", rental.getUser().getId());
            map.put("userName", rental.getUser().getUserName());
        } else {
            map.put("userId", "-");
            map.put("userName", "-");
        }
        return map;
    }

    public CommonResponse<List<Map<String, Object>>> getRentals(CommonSearchRequest request) {
        Page<Rental> page = rentalRepository.findAll(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream()
                .filter(r -> request.getIncludeDeleted() || !r.getIsDeleted())
                .filter(r -> {
                    String sf = request.getSearchField();
                    String sk = request.getSearchKeyword();

                    if (sk == null || sk.isEmpty()) return true;

                    // 💡 새롭게 추가된 드롭다운 필터 조건 완벽 매핑
                    if ("branchName".equals(sf)) {
                        return r.getBranch() != null && r.getBranch().getBranchName() != null && r.getBranch().getBranchName().contains(sk);
                    } else if ("deviceId".equals(sf)) {
                        return r.getDevice() != null && r.getDevice().getDeviceId() != null && r.getDevice().getDeviceId().contains(sk);
                    } else if ("modelName".equals(sf)) {
                        return r.getDevice() != null && r.getDevice().getModelVersion() != null
                                && r.getDevice().getModelVersion().getModel() != null
                                && r.getDevice().getModelVersion().getModel().getModelName().contains(sk);
                    } else if ("userName".equals(sf)) {
                        return r.getUser() != null && r.getUser().getUserName() != null && r.getUser().getUserName().contains(sk);
                    } else if ("userId".equals(sf)) {
                        return r.getUser() != null && r.getUser().getId().toString().equals(sk);
                    } else if ("all".equals(sf)) {
                        // 통합 검색일 경우 위 조건들을 모두 검사 (하나라도 걸리면 통과)
                        boolean matchBranch = r.getBranch() != null && r.getBranch().getBranchName() != null && r.getBranch().getBranchName().contains(sk);
                        boolean matchDevice = r.getDevice() != null && r.getDevice().getDeviceId() != null && r.getDevice().getDeviceId().contains(sk);
                        boolean matchModel = r.getDevice() != null && r.getDevice().getModelVersion() != null
                                && r.getDevice().getModelVersion().getModel() != null
                                && r.getDevice().getModelVersion().getModel().getModelName().contains(sk);
                        boolean matchUser = r.getUser() != null && r.getUser().getUserName() != null && r.getUser().getUserName().contains(sk);
                        boolean matchUserId = r.getUser() != null && r.getUser().getId().toString().equals(sk);
                        return matchBranch || matchDevice || matchModel || matchUser || matchUserId;
                    }
                    return true;
                })
                .map(this::convertToMap)
                .collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> createRental(Map<String, Object> body) {
        Long branchId = Long.valueOf(body.get("branchId").toString());
        Long deviceId = Long.valueOf(body.get("deviceId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());

        Device device = deviceRepository.findByIdAndIsDeletedFalse(deviceId)
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "기기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Rental rental = Rental.builder()
                .device(device)
                .branch(branchRepository.findByIdAndIsDeletedFalse(branchId).orElse(null))
                .user(userRepository.findById(userId).orElse(null))
                .status("APPLIED") // 기획서 첫 단계 '신청'
                .applyDate(LocalDate.now())
                .isDeleted(false)
                .build();

        rentalRepository.save(rental);
        return CommonResponse.created(convertToMap(rental));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> updateRental(Long id, Map<String, Object> body) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (body.containsKey("returnDate") && body.get("returnDate") != null) {
            rental.setReturnDate(LocalDate.parse(body.get("returnDate").toString()));
            rental.setStatus("RETURNED");

            Device device = rental.getDevice();
            if (device != null) {
                device.setStatus("RENTAL_READY");
            }
        }
        return CommonResponse.success(convertToMap(rental));
    }

    @Transactional
    public void deleteRental(Long id) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        rental.setIsDeleted(true);
    }

    // 모달 호출용: 특정 유저의 임대 이력 조회
    public CommonResponse<List<Map<String, Object>>> getUserRentals(Long userId, CommonSearchRequest request) {
        Page<Rental> page = rentalRepository.findAll(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream()
                .filter(r -> !r.getIsDeleted() && r.getUser() != null && r.getUser().getId().equals(userId))
                .map(this::convertToMap)
                .collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    // 좌측 패널용: 총수량과 임대중 수량 집계
    public List<Map<String, Object>> getRentalSummaryByBranch() {
        return branchRepository.findAllByIsDeletedFalse(PageRequest.of(0, 1000))
                .getContent().stream().map(branch -> {
                    List<Rental> rentals = rentalRepository.findAllByBranchIdAndIsDeletedFalse(branch.getId());
                    long rentingCount = rentals.stream().filter(r -> "RENTING".equals(r.getStatus())).count();

                    return Map.<String, Object>of(
                            "branchId", branch.getId(),
                            "branchName", branch.getBranchName(),
                            "totalCount", rentals.size(),
                            "rentingCount", rentingCount
                    );
                }).collect(Collectors.toList());
    }
}