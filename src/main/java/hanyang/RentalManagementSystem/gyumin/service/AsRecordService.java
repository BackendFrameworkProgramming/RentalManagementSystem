package hanyang.RentalManagementSystem.gyumin.service;

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
import java.util.HashMap;
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
            throw new CustomException("INVALID_STATUS_TRANSITION", current + " → " + next + " 전이는 허용되지 않습니다.");
        }
    }

    private Map<String, Object> convertToMap(AsRecord asRecord) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", asRecord.getId());
        map.put("status", asRecord.getStatus());
        map.put("receiptDate", asRecord.getReceiptDate());
        map.put("completeDate", asRecord.getCompleteDate());

        if (asRecord.getDevice() != null) {
            map.put("device", Map.of("id", asRecord.getDevice().getId(), "deviceId", asRecord.getDevice().getDeviceId()));
        }
        if (asRecord.getBranch() != null) {
            map.put("branch", Map.of("id", asRecord.getBranch().getId(), "branchName", asRecord.getBranch().getBranchName()));
        }
        if (asRecord.getVendor() != null) {
            map.put("vendor", Map.of("id", asRecord.getVendor().getId(), "vendorName", asRecord.getVendor().getVendorName()));
        }
        if (asRecord.getAsType() != null) {
            map.put("asType", Map.of("id", asRecord.getAsType().getId(), "typeName", asRecord.getAsType().getTypeName()));
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getAsRecords(CommonSearchRequest request) {
        return asRecordRepository.findAllByIsDeletedFalse(request.toPageable()).map(this::convertToMap);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAsRecord(Long id) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "AS 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return convertToMap(asRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createAsRecord(Map<String, Object> body) {
        String deviceIdStr = body.get("deviceId").toString();

        Device device = deviceRepository.findAllByIsDeletedFalse().stream()
                .filter(d -> deviceIdStr.equals(d.getDeviceId()))
                .findFirst()
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "해당 기기 번호(" + deviceIdStr + ")를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

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

        if (body.containsKey("status") && body.get("status") != null) {
            asRecord.setStatus(body.get("status").toString());
        }

        if (body.containsKey("completeDate") && body.get("completeDate") != null) {
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
        List<AsRecord> remainingRecords = asRecordRepository.findAllByDeviceIdAndIsDeletedFalse(device.getId());
        LocalDate latestAsDate = remainingRecords.stream()
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
            return Map.<String, Object>of("vendorId", vendor.getId(), "vendorName", vendor.getVendorName(), "count", asRecords.size());
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getVendors(CommonSearchRequest request) {
        return vendorRepository.findAllByIsDeletedFalse(request.toPageable())
                .map(vendor -> Map.<String, Object>of("id", vendor.getId(), "vendorName", vendor.getVendorName()));
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createVendor(Map<String, Object> body) {
        Vendor vendor = Vendor.builder().vendorName((String) body.get("vendorName")).isDeleted(false).build();
        vendorRepository.save(vendor);
        body.put("id", vendor.getId());
        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateVendor(Long id, Map<String, Object> body) {
        Vendor vendor = vendorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("VENDOR_NOT_FOUND", "수리 업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (body.containsKey("vendorName")) vendor.setVendorName((String) body.get("vendorName"));
        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("VENDOR_NOT_FOUND", "수리 업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        vendor.setIsDeleted(true);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getAsTypes(CommonSearchRequest request) {
        return asTypeRepository.findAllByUseYnTrue(request.toPageable())
                .map(type -> Map.<String, Object>of("id", type.getId(), "typeName", type.getTypeName(), "description", type.getDescription()));
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createAsType(Map<String, Object> body) {
        AsType asType = AsType.builder().typeName((String) body.get("typeName")).description((String) body.get("description")).useYn(true).build();
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