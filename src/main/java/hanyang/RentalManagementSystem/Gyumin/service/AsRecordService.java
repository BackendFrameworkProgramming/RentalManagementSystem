package hanyang.RentalManagementSystem.Gyumin.service;

import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.entity.*;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AsRecordService {

    private final AsRecordRepository asRecordRepository;
    private final DeviceRepository deviceRepository;
    private final VendorRepository vendorRepository;
    private final AsTypeRepository asTypeRepository;

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

    // === AsRecord Methods ===

    @Transactional(readOnly = true)
    public Page<AsRecord> getAsRecords(CommonSearchRequest request) {
        return asRecordRepository.findAllByIsDeletedFalse(request.toPageable());
    }

    @Transactional(readOnly = true)
    public AsRecord getAsRecord(Long id) {
        return asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "AS 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createAsRecord(Map<String, Object> body) {
        Long deviceId = Long.valueOf(body.get("deviceId").toString());
        Device device = deviceRepository.findByIdAndIsDeletedFalse(deviceId)
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "디바이스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        validateStatusTransition(device.getStatus(), "AS_RECEIVED");
        device.setStatus("AS_RECEIVED");
        device.setLatestAsDate(LocalDate.now());

        AsRecord asRecord = AsRecord.builder()
                .device(device)
                .branch(device.getBranch())
                .status("RECEIVED")
                .receiptDate(LocalDate.now())
                .isDeleted(false)
                .build();

        if (body.containsKey("vendorId")) {
            vendorRepository.findByIdAndIsDeletedFalse(Long.valueOf(body.get("vendorId").toString()))
                    .ifPresent(asRecord::setVendor);
        }

        if (body.containsKey("asTypeId")) {
            asTypeRepository.findById(Long.valueOf(body.get("asTypeId").toString()))
                    .ifPresent(asRecord::setAsType);
        }

        asRecordRepository.save(asRecord);
        body.put("id", asRecord.getId());
        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateAsRecord(Long id, Map<String, Object> body) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "AS 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Device device = asRecord.getDevice();

        if (body.containsKey("status")) {
            asRecord.setStatus(body.get("status").toString());
        }

        if (body.containsKey("completeDate")) {
            asRecord.setCompleteDate(LocalDate.parse(body.get("completeDate").toString()));

            String nextStatus = body.containsKey("isDisposed") && (Boolean) body.get("isDisposed") ? "DISPOSED" : "RENTAL_READY";
            validateStatusTransition(device.getStatus(), nextStatus);
            device.setStatus(nextStatus);
        }

        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAsRecord(Long id) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "AS 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        asRecord.setIsDeleted(true);

        Device device = asRecord.getDevice();
        List<AsRecord> remainingAsRecords = asRecordRepository.findAllByDeviceIdAndIsDeletedFalse(device.getId());

        LocalDate latestAsDate = remainingAsRecords.stream()
                .filter(record -> !record.getIsDeleted() && record.getReceiptDate() != null)
                .map(AsRecord::getReceiptDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        device.setLatestAsDate(latestAsDate);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAsSummaryByVendor() {
        Page<Vendor> vendorsPage = vendorRepository.findAllByIsDeletedFalse(PageRequest.of(0, 1000));
        return vendorsPage.getContent().stream().map(vendor -> {
            List<AsRecord> asRecords = asRecordRepository.findAllByVendorIdAndIsDeletedFalse(vendor.getId());
            return Map.<String, Object>of(
                    "vendorId", vendor.getId(),
                    "vendorName", vendor.getVendorName(),
                    "count", asRecords.size()
            );
        }).collect(Collectors.toList());
    }

    // === Vendor Methods ===

    @Transactional(readOnly = true)
    public Page<Vendor> getVendors(CommonSearchRequest request) {
        return vendorRepository.findAllByIsDeletedFalse(request.toPageable());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createVendor(Map<String, Object> body) {
        Vendor vendor = Vendor.builder()
                .vendorName((String) body.get("vendorName"))
                .contact((String) body.get("contact"))
                .address((String) body.get("address"))
                .status("ACTIVE")
                .isDeleted(false)
                .build();
        vendorRepository.save(vendor);
        body.put("id", vendor.getId());
        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateVendor(Long id, Map<String, Object> body) {
        Vendor vendor = vendorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("VENDOR_NOT_FOUND", "업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (body.containsKey("vendorName")) vendor.setVendorName((String) body.get("vendorName"));
        if (body.containsKey("contact")) vendor.setContact((String) body.get("contact"));
        if (body.containsKey("status")) vendor.setStatus(body.get("status").toString());

        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("VENDOR_NOT_FOUND", "업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        vendor.setIsDeleted(true);
    }

    // === AsType Methods ===

    @Transactional(readOnly = true)
    public Page<AsType> getAsTypes(CommonSearchRequest request) {
        return asTypeRepository.findAllByUseYnTrue(request.toPageable());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createAsType(Map<String, Object> body) {
        AsType asType = AsType.builder()
                .typeName((String) body.get("typeName"))
                .description((String) body.get("description"))
                .useYn(true)
                .build();
        asTypeRepository.save(asType);
        body.put("id", asType.getId());
        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateAsType(Long id, Map<String, Object> body) {
        AsType asType = asTypeRepository.findById(id)
                .orElseThrow(() -> new CustomException("AS_TYPE_NOT_FOUND", "AS 유형을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (body.containsKey("typeName")) asType.setTypeName((String) body.get("typeName"));
        if (body.containsKey("useYn")) asType.setUseYn(Boolean.parseBoolean(body.get("useYn").toString()));

        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAsType(Long id) {
        AsType asType = asTypeRepository.findById(id)
                .orElseThrow(() -> new CustomException("AS_TYPE_NOT_FOUND", "AS 유형을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        asType.setUseYn(false);
    }
}