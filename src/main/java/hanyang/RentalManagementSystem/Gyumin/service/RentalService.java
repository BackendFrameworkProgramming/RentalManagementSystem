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
            throw new CustomException("INVALID_STATUS_TRANSITION",
                    current + " → " + next + " 전이는 허용되지 않습니다.");
        }
    }

    @Transactional(readOnly = true)
    public Page<Rental> getRentals(CommonSearchRequest request) {
        return rentalRepository.findAllByIsDeletedFalse(request.toPageable());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createRental(Map<String, Object> body) {
        Long deviceId = Long.valueOf(body.get("deviceId").toString());
        Long branchId = Long.valueOf(body.get("branchId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());

        Device device = deviceRepository.findByIdAndIsDeletedFalse(deviceId)
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "디바이스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        validateStatusTransition(device.getStatus(), "RENTING");
        device.setStatus("RENTING");
        device.setLatestRentalDate(LocalDate.now());

        Rental rental = Rental.builder()
                .device(device)
                .branch(branch)
                .user(user)
                .status("RENTING")
                .applyDate(LocalDate.now())
                .isDeleted(false)
                .build();

        rentalRepository.save(rental);
        body.put("id", rental.getId());
        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateRental(Long id, Map<String, Object> body) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Device device = rental.getDevice();

        if (body.containsKey("returnDate")) {
            rental.setReturnDate(LocalDate.parse(body.get("returnDate").toString()));
            rental.setStatus("RETURNED");

            String currentStatus = device.getStatus();
            if (!"AS_RECEIVED".equals(currentStatus) && !"AS_PROGRESS".equals(currentStatus)) {
                validateStatusTransition(currentStatus, "RENTAL_READY");
                device.setStatus("RENTAL_READY");
            }
        }

        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRental(Long id) {
        Rental rental = rentalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("RENTAL_NOT_FOUND", "임대 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        rental.setIsDeleted(true);

        Device device = rental.getDevice();
        List<Rental> remainingRentals = rentalRepository.findAllByDeviceIdAndIsDeletedFalse(device.getId());

        LocalDate latestRentalDate = remainingRentals.stream()
                .filter(record -> !record.getIsDeleted() && record.getApplyDate() != null)
                .map(Rental::getApplyDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        device.setLatestRentalDate(latestRentalDate);
    }

    @Transactional(readOnly = true)
    public Page<Rental> getUserRentals(Long userId, CommonSearchRequest request) {
        return rentalRepository.findAllByUserIdAndIsDeletedFalse(userId, request.toPageable());
    }

    @Transactional(readOnly = true)
    public List<Device> getAvailableDevicesByBranch(Long branchId) {
        return deviceRepository.findAllByBranchIdAndIsDeletedFalse(branchId).stream()
                .filter(device -> "RENTAL_READY".equals(device.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRentalSummaryByBranch() {
        List<Branch> branches = branchRepository.findAllByIsDeletedFalse();
        return branches.stream().map(branch -> {
            List<Rental> rentals = rentalRepository.findAllByBranchIdAndIsDeletedFalse(branch.getId());
            return Map.<String, Object>of(
                    "branchId", branch.getId(),
                    "branchName", branch.getBranchName(),
                    "count", rentals.size()
            );
        }).collect(Collectors.toList());
    }
}