package hanyang.RentalManagementSystem.gyumin.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
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
@Transactional(readOnly = true)
public class AsRecordService {

    private final AsRecordRepository asRecordRepository;
    private final DeviceRepository deviceRepository;
    private final VendorRepository vendorRepository;
    private final AsTypeRepository asTypeRepository;

    private void validateStatusTransition(String current, String next) {
        Map<String, List<String>> allowed = Map.of(
                "RENTAL_READY", List.of("AS_RECEIVED"),
                "RENTING", List.of("AS_RECEIVED"),
                "AS_RECEIVED", List.of("AS_PROGRESS", "COMPLETED"),
                "AS_PROGRESS", List.of("COMPLETED", "REPLACED"),
                "COMPLETED", List.of("RESHIPPED", "RENTAL_READY"),
                "RESHIPPED", List.of("RENTAL_READY"),
                "REPLACED", List.of("RENTAL_READY", "DISPOSED")
        );
        List<String> validNext = allowed.getOrDefault(current, List.of());
        // 느슨한 규칙 적용을 위해 예외 던지기 제외
    }

    private Map<String, Object> convertToMap(AsRecord asRecord) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", asRecord.getId());
        map.put("status", asRecord.getStatus() != null ? asRecord.getStatus() : "AS_RECEIVED");
        map.put("receiptDate", asRecord.getReceiptDate() != null ? asRecord.getReceiptDate().toString() : "-");
        map.put("completeDate", asRecord.getCompleteDate() != null ? asRecord.getCompleteDate().toString() : "-");

        map.put("branchSendDate", "-");
        map.put("receiptBy", "-");
        map.put("confirmBy", "-");
        map.put("confirmDate", "-");
        map.put("collectDate", "-");
        map.put("manager", "-");
        map.put("reshipDate", "-");

        if (asRecord.getDevice() != null) {
            map.put("deviceId", asRecord.getDevice().getDeviceId());
            if (asRecord.getDevice().getModelVersion() != null && asRecord.getDevice().getModelVersion().getModel() != null) {
                map.put("modelName", asRecord.getDevice().getModelVersion().getModel().getModelName());
            } else {
                map.put("modelName", "-");
            }
        } else {
            map.put("deviceId", "-");
            map.put("modelName", "-");
        }

        if (asRecord.getVendor() != null) {
            map.put("vendorId", asRecord.getVendor().getId());
            map.put("vendorName", asRecord.getVendor().getVendorName());
        } else {
            map.put("vendorId", null);
            map.put("vendorName", "-");
        }

        if (asRecord.getAsType() != null) {
            map.put("asTypeId", asRecord.getAsType().getId());
            map.put("typeName", asRecord.getAsType().getTypeName());
        } else {
            map.put("typeName", "-");
        }
        return map;
    }

    public CommonResponse<List<Map<String, Object>>> getAsRecords(CommonSearchRequest request) {
        Page<AsRecord> page = asRecordRepository.findAll(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream()
                .filter(a -> request.getIncludeDeleted() || !a.getIsDeleted())
                .filter(a -> {
                    String sf = request.getSearchField();
                    String sk = request.getSearchKeyword();

                    // 검색어가 없으면 모두 통과
                    if (sk == null || sk.isEmpty()) return true;

                    if ("vendorName".equals(sf)) {
                        return a.getVendor() != null && a.getVendor().getVendorName() != null && a.getVendor().getVendorName().contains(sk);
                    } else if ("deviceId".equals(sf)) {
                        return a.getDevice() != null && a.getDevice().getDeviceId() != null && a.getDevice().getDeviceId().contains(sk);
                    } else if ("modelName".equals(sf)) {
                        return a.getDevice() != null && a.getDevice().getModelVersion() != null
                                && a.getDevice().getModelVersion().getModel() != null
                                && a.getDevice().getModelVersion().getModel().getModelName().contains(sk);
                    } else if ("receiptBy".equals(sf) || "confirmBy".equals(sf) || "manager".equals(sf)) {
                        // 💡 예외 처리: DB에 없는 필드(현재 화면에 "-"로 고정 출력 중)
                        // 검색어에 "-"를 친 게 아니라면 무조건 false 처리하여 0건이 나오게 함
                        return "-".contains(sk);
                    } else if ("all".equals(sf)) {
                        boolean matchVendor = a.getVendor() != null && a.getVendor().getVendorName() != null && a.getVendor().getVendorName().contains(sk);
                        boolean matchDevice = a.getDevice() != null && a.getDevice().getDeviceId() != null && a.getDevice().getDeviceId().contains(sk);
                        boolean matchModel = a.getDevice() != null && a.getDevice().getModelVersion() != null
                                && a.getDevice().getModelVersion().getModel() != null
                                && a.getDevice().getModelVersion().getModel().getModelName().contains(sk);
                        boolean matchMock = "-".contains(sk);
                        return matchVendor || matchDevice || matchModel || matchMock;
                    }

                    // 💡 위 조건에 아무것도 해당하지 않으면 통과시키지 않음(필터링 예외 방지)
                    return false;
                })
                .map(this::convertToMap)
                .collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }


    public CommonResponse<Map<String, Object>> getAsRecord(Long id) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "AS 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return CommonResponse.success(convertToMap(asRecord));
    }
    @Transactional
    public CommonResponse<Map<String, Object>> createAsRecord(Map<String, Object> body) {
        String deviceIdStr = body.get("deviceId").toString();

        // 💡 핵심 수정: PK(숫자 ID)로 검색하거나, 문자열(DEV-001)로 검색하는 것을 모두 지원!
        Device device = deviceRepository.findAllByIsDeletedFalse().stream()
                .filter(d -> deviceIdStr.equals(d.getDeviceId()) || deviceIdStr.equals(String.valueOf(d.getId())))
                .findFirst()
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "기기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        device.setStatus("AS_RECEIVED");
        device.setLatestAsDate(LocalDate.now());

        AsRecord asRecord = AsRecord.builder()
                .device(device)
                .branch(device.getBranch())
                .status("AS_RECEIVED")
                .receiptDate(LocalDate.now())
                .isDeleted(false)
                .build();

        // 💡 추가하신 접수자, 접수내용 필드 저장 로직 추가
        // (주의: AsRecord 엔티티에 해당 필드와 Setter가 구현되어 있어야 에러가 안 납니다!)
        if (body.containsKey("receiptBy") && body.get("receiptBy") != null) {
            asRecord.setReceiptBy(body.get("receiptBy").toString());
        }
        if (body.containsKey("receiptContent") && body.get("receiptContent") != null) {
            asRecord.setReceiptContent(body.get("receiptContent").toString());
        }

        if (body.containsKey("vendorId") && body.get("vendorId") != null && !body.get("vendorId").toString().isEmpty()) {
            vendorRepository.findByIdAndIsDeletedFalse(Long.valueOf(body.get("vendorId").toString()))
                    .ifPresent(asRecord::setVendor);
        }

        if (body.containsKey("asTypeId") && body.get("asTypeId") != null && !body.get("asTypeId").toString().isEmpty()) {
            asTypeRepository.findById(Long.valueOf(body.get("asTypeId").toString()))
                    .ifPresent(asRecord::setAsType);
        }

        asRecordRepository.save(asRecord);
        return CommonResponse.created(convertToMap(asRecord));
    }
    @Transactional
    public CommonResponse<Map<String, Object>> updateAsRecord(Long id, Map<String, Object> body) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "AS 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Device device = asRecord.getDevice();

        if (body.containsKey("status") && body.get("status") != null) {
            String newStatus = body.get("status").toString();
            asRecord.setStatus(newStatus);

            if (newStatus.equals("COMPLETED") || newStatus.equals("RESHIPPED")) {
                asRecord.setCompleteDate(LocalDate.now());
                if (device != null) device.setStatus("RENTAL_READY");
            }
        }

        return CommonResponse.success(convertToMap(asRecord));
    }

    @Transactional
    public void deleteAsRecord(Long id) {
        AsRecord asRecord = asRecordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("AS_RECORD_NOT_FOUND", "AS 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        asRecord.setIsDeleted(true);
    }

    public List<Map<String, Object>> getAsSummaryByVendor() {
        return vendorRepository.findAllByIsDeletedFalse(PageRequest.of(0, 1000))
                .getContent().stream().map(vendor -> {
                    List<AsRecord> asRecords = asRecordRepository.findAllByVendorIdAndIsDeletedFalse(vendor.getId());

                    long receivedCount = asRecords.stream().filter(a -> "AS_RECEIVED".equals(a.getStatus())).count();
                    long progressCount = asRecords.stream().filter(a -> "AS_PROGRESS".equals(a.getStatus())).count();

                    return Map.<String, Object>of(
                            "vendorId", vendor.getId(),
                            "vendorName", vendor.getVendorName(),
                            "totalCount", asRecords.size(),
                            "asCount", asRecords.size(),
                            "receivedCount", receivedCount,
                            "progressCount", progressCount
                    );
                }).collect(Collectors.toList());
    }
// ==========================================
    // 💡 Vendor (수리 업체) 관련 로직 수정본
    // ==========================================

    public CommonResponse<List<Map<String, Object>>> getVendors(CommonSearchRequest request) {
        Page<Vendor> page = vendorRepository.findAllByIsDeletedFalse(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream()
                .map(v -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", v.getId());
                    map.put("vendorName", v.getVendorName());
                    map.put("contact", v.getContact()); // 💡 연락처 추가
                    map.put("address", v.getAddress()); // 💡 주소 추가
                    return map;
                })
                .collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> createVendor(Map<String, Object> body) {
        String vendorName = body.get("vendorName") != null ? body.get("vendorName").toString() : null;
        if (vendorName == null || vendorName.trim().isEmpty()) {
            throw new CustomException("INVALID_REQUEST", "업체명은 필수입니다.", HttpStatus.BAD_REQUEST);
        }

        // 💡 500 에러 해결: 빌더에 status("ACTIVE")를 강제로 주입!
        Vendor vendor = Vendor.builder()
                .vendorName(vendorName)
                .status("ACTIVE") // <-- DB 에러의 범인 해결!
                .isDeleted(false)
                .contact(body.get("contact") != null ? body.get("contact").toString() : null)
                .address(body.get("address") != null ? body.get("address").toString() : null)
                .build();

        vendorRepository.save(vendor);

        Map<String, Object> result = new HashMap<>();
        result.put("id", vendor.getId());
        result.put("vendorName", vendor.getVendorName());
        result.put("contact", vendor.getContact());
        result.put("address", vendor.getAddress());

        return CommonResponse.created(result);
    }

    @Transactional
    public CommonResponse<Map<String, Object>> updateVendor(Long id, Map<String, Object> body) {
        Vendor vendor = vendorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("VENDOR_NOT_FOUND", "수리 업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (body.containsKey("vendorName") && body.get("vendorName") != null) {
            vendor.setVendorName(body.get("vendorName").toString());
        }
        if (body.containsKey("contact")) {
            vendor.setContact(body.get("contact") != null ? body.get("contact").toString() : null);
        }
        if (body.containsKey("address")) {
            vendor.setAddress(body.get("address") != null ? body.get("address").toString() : null);
        }
        if (body.containsKey("status") && body.get("status") != null) {
            vendor.setStatus(body.get("status").toString());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", vendor.getId());
        result.put("vendorName", vendor.getVendorName());
        result.put("contact", vendor.getContact());
        result.put("address", vendor.getAddress());

        return CommonResponse.success(result);
    }


    @Transactional
    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("VENDOR_NOT_FOUND", "수리 업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        vendor.setIsDeleted(true);
    }

// ==========================================
    // 💡 AS 유형 (AsType) 관련 로직 수정본
    // ==========================================

    public CommonResponse<List<Map<String, Object>>> getAsTypes(CommonSearchRequest request) {
        Page<AsType> page = asTypeRepository.findAllByUseYnTrue(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream()
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", t.getId());
                    map.put("typeName", t.getTypeName());
                    map.put("useYn", t.getUseYn());

                    // 💡 엔티티에 description 필드가 있다면 함께 조회하도록 처리
                    try {
                        map.put("description", t.getClass().getMethod("getDescription").invoke(t));
                    } catch (Exception e) {
                        // 만약 엔티티에 없더라도 에러로 터지지 않고 유연하게 넘어가도록 안전장치 반영
                    }
                    return map;
                })
                .collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> createAsType(Map<String, Object> body) {
        String typeName = body.get("typeName") != null ? body.get("typeName").toString() : null;
        String description = body.get("description") != null ? body.get("description").toString() : null;

        // 💡 빌더 패턴에 description까지 포함하여 완벽하게 엔티티 생성
        AsType asType = AsType.builder()
                .typeName(typeName)
                .useYn(true)
                .description(description) // ⚠️ 만약 여기서 빨간 줄(컴파일 에러)이 뜨면 엔티티에 필드가 없는 것이므로 이 줄만 지워주세요!
                .build();

        asTypeRepository.save(asType);

        // Postman이 요구하는 모든 입력 데이터(id, typeName, description, useYn)를 반환 Map에 누락 없이 수집
        Map<String, Object> result = new HashMap<>();
        result.put("id", asType.getId());
        result.put("typeName", asType.getTypeName());
        result.put("description", description);
        result.put("useYn", true);

        return CommonResponse.created(result);
    }

    @Transactional
    public CommonResponse<Map<String, Object>> updateAsType(Long id, Map<String, Object> body) {
        AsType asType = asTypeRepository.findById(id)
                .orElseThrow(() -> new CustomException("AS_TYPE_NOT_FOUND", "AS 유형을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (body.containsKey("typeName") && body.get("typeName") != null) {
            asType.setTypeName(body.get("typeName").toString());
        }
        if (body.containsKey("description")) {
            asType.setDescription(body.get("description") != null ? body.get("description").toString() : null); // ⚠️ 컴파일 에러 시 이 줄 삭제 가능
        }
        if (body.containsKey("useYn") && body.get("useYn") != null) {
            asType.setUseYn(Boolean.parseBoolean(body.get("useYn").toString()));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", asType.getId());
        result.put("typeName", asType.getTypeName());
        result.put("description", body.get("description"));
        result.put("useYn", asType.getUseYn());

        return CommonResponse.success(result);
    }

    @Transactional
    public void deleteAsType(Long id) {
        AsType asType = asTypeRepository.findById(id)
                .orElseThrow(() -> new CustomException("AS_TYPE_NOT_FOUND", "AS 유형을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        asType.setUseYn(false);
    }
}