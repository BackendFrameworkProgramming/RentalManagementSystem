package hanyang.RentalManagementSystem.Gyumin.service;

import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.entity.*;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
public class RentalService {

    private final RentalRepository rentalRepository;
    private final DeviceRepository deviceRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

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
            throw new CustomException("INVALID_STATUS_TRANSITION", current + " → " + next + " 전이는 허용되지 않습니다.");
        }
    }

    private Map<String, Object> convertToMap(Rental rental) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", rental.getId());
        map.put("status", rental.getStatus());
        map.put("applyDate", rental.getApplyDate());
        map.put("returnDate", rental.getReturnDate());
        if (rental.getDevice() != null) {
            map.put("device", Map.of("id", rental.getDevice().getId(), "deviceId", rental.getDevice().getDeviceId()));
        }
        if (rental.getBranch() != null) {
            map.put("branch", Map.of("id", rental.getBranch().getId(), "branchName", rental.getBranch().getBranchName()));
        }
        if (rental.getUser() != null) {
            map.put("user", Map.of("id", rental.getUser().getId(), "userName", rental.getUser().getUserName()));
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getRentals(CommonSearchRequest request) {
        return rentalRepository.findAllByIsDeletedFalse(request.toPageable()).map(this::convertToMap);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getUserRentals(Long userId, CommonSearchRequest request) {
        return rentalRepository.findAllByUserIdAndIsDeletedFalse(userId, request.toPageable()).map(this::convertToMap);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAvailableDevicesByBranch(Long branchId) {
        // [수정] 진짜 DB 로직으로 원복하되 Map으로 안전하게 반환
        return deviceRepository.findAllByBranchIdAndIsDeletedFalse(branchId).stream()
                .filter(device -> "RENTAL_READY".equals(device.getStatus()))
                .map(device -> Map.<String, Object>of(
                        "id", device.getId(),
                        "deviceId", device.getDeviceId(),
                        "status", device.getStatus()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createRental(Map<String, Object> body) {
        Long deviceId = Long.valueOf(body.get("deviceId").toString());
        Long branchId = Long.valueOf(body.get("branchId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());

        Device device = deviceRepository.findByIdAndIsDeletedFalse(deviceId).orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "기기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(branchId).orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        User user = userRepository.findByIdAndIsDeletedFalse(userId).orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        validateStatusTransition(device.getStatus(), "RENTING");
        device.setStatus("RENTING");
        device.setLatestRentalDate(LocalDate.now());

        Rental rental = Rental.builder().device(device).branch(branch).user(user).status("RENTING").applyDate(LocalDate.now()).isDeleted(false).build();
        rentalRepository.save(rental);
        return convertToMap(rental);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateRental(Long id, Map<String, Object> body) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (body.containsKey("returnDate") && body.get("returnDate") != null) {
            rental.setReturnDate(LocalDate.parse(body.get("returnDate").toString()));
            rental.setStatus("RETURNED");
            Device d = rental.getDevice();
            if (!"AS_PROGRESS".equals(d.getStatus())) d.setStatus("RENTAL_READY");
        }
        return convertToMap(rental);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRental(Long id) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        rental.setIsDeleted(true);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRentalSummaryByBranch() {
        return branchRepository.findAllByIsDeletedFalse().stream().map(branch -> {
            long count = rentalRepository.findAllByBranchIdAndIsDeletedFalse(branch.getId()).size();
            return Map.<String, Object>of("branchId", branch.getId(), "branchName", branch.getBranchName(), "count", count);
        }).collect(Collectors.toList());
    }
}