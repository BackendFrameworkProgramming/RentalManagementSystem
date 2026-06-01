package hanyang.RentalManagementSystem.eunhye.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.BiometricData;
import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.entity.EmergencyRecord;
import hanyang.RentalManagementSystem.common.entity.Rental;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.BiometricDataRepository;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.EmergencyRecordRepository;
import hanyang.RentalManagementSystem.common.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BiometricService {

    private final BiometricDataRepository biometricDataRepository;
    private final EmergencyRecordRepository emergencyRecordRepository;
    private final RentalRepository rentalRepository;
    private final DeviceRepository deviceRepository;

    @Transactional(readOnly = true)
    public CommonResponse<Map<String, Object>> getBiometricDataList(int page, int size) {

        Page<BiometricData> biometricPage =
                biometricDataRepository.findAllByIsDeletedFalse(
                        PageRequest.of(page - 1, size)
                );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("biometricData",
                biometricPage.getContent().stream().map(this::toBiometricMap).toList());

        return CommonResponse.success(data, Pagination.of(biometricPage));
    }

    @Transactional(readOnly = true)
    public CommonResponse<Map<String, Object>> getBiometricDataDetail(Long id) {

        BiometricData biometricData = biometricDataRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "BIOMETRIC_NOT_FOUND",
                        "생체정보를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        List<EmergencyRecord> emergencyRecords =
                emergencyRecordRepository.findAllByBiometricDataId(id);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("biometricData", toBiometricMap(biometricData));
        data.put("emergencyRecords",
                emergencyRecords.stream().map(this::toEmergencyMap).toList());

        return CommonResponse.success(data);
    }

    public CommonResponse<Map<String, Object>> createBiometricData(Map<String, Object> body) {

        Long deviceId = toLong(body.get("deviceId"));

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException(
                        "DEVICE_NOT_FOUND",
                        "디바이스를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        Rental rental = rentalRepository
                .findByDeviceIdAndStatusAndIsDeletedFalse(deviceId, "RENTING")
                .orElse(null);

        BiometricData biometricData = BiometricData.builder()
                .device(device)
                .rental(rental)
                .userName(toString(body.get("userName")))
                .latestUseDate(toLocalDate(body.get("latestUseDate")))
                .latestUseTime(toString(body.get("latestUseTime")))
                .useTimePerDay(toString(body.get("useTimePerDay")))
                .breathPerDay(toInteger(body.get("breathPerDay")))
                .stepsPerDay(toInteger(body.get("stepsPerDay")))
                .totalUseTime(toString(body.get("totalUseTime")))
                .totalSteps(toInteger(body.get("totalSteps")))
                .latestUpdateTime(LocalDateTime.now())
                .latestLocation(toString(body.get("latestLocation")))
                .isDeleted(false)
                .build();

        BiometricData saved = biometricDataRepository.save(biometricData);

        return CommonResponse.created(toBiometricMap(saved));
    }

    public CommonResponse<Map<String, Object>> deleteBiometricData(Long id) {

        BiometricData biometricData = biometricDataRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "BIOMETRIC_NOT_FOUND",
                        "생체정보를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        biometricData.setIsDeleted(true);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("message", "생체정보가 삭제되었습니다.");

        return CommonResponse.success(data);
    }

    @Transactional(readOnly = true)
    public CommonResponse<Map<String, Object>> getEmergencyRecords(int page, int size) {

        Page<EmergencyRecord> emergencyPage =
                emergencyRecordRepository.findAll(PageRequest.of(page - 1, size));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("emergencyRecords",
                emergencyPage.getContent().stream().map(this::toEmergencyMap).toList());

        return CommonResponse.success(data, Pagination.of(emergencyPage));
    }

    public CommonResponse<Map<String, Object>> createEmergencyRecord(Map<String, Object> body) {

        Long biometricDataId = toLong(body.get("biometricDataId"));

        BiometricData biometricData = biometricDataRepository.findById(biometricDataId)
                .orElseThrow(() -> new CustomException(
                        "BIOMETRIC_NOT_FOUND",
                        "생체정보를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        EmergencyRecord emergencyRecord = EmergencyRecord.builder()
                .biometricData(biometricData)
                .rental(biometricData.getRental())
                .emergencyType(toString(body.get("emergencyType")))
                .emergencyRecordTime(LocalDateTime.now())
                .actionContent(toString(body.get("actionContent")))
                .actionResult(toString(body.get("actionResult")))
                .build();

        EmergencyRecord saved = emergencyRecordRepository.save(emergencyRecord);

        return CommonResponse.created(toEmergencyMap(saved));
    }

    public CommonResponse<Map<String, Object>> updateEmergencyRecord(Long id, Map<String, Object> body) {

        EmergencyRecord emergencyRecord = emergencyRecordRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "EMERGENCY_NOT_FOUND",
                        "응급기록을 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        if (body.containsKey("actionContent")) {
            emergencyRecord.setActionContent(toString(body.get("actionContent")));
        }

        if (body.containsKey("actionResult")) {
            emergencyRecord.setActionResult(toString(body.get("actionResult")));
        }

        return CommonResponse.success(toEmergencyMap(emergencyRecord));
    }

    @Transactional(readOnly = true)
    public CommonResponse<Map<String, Object>> getSummaryByModel() {

        Map<String, Long> summary = new LinkedHashMap<>();

        for (Object[] row : biometricDataRepository.countByModelName()) {
            String modelName = row[0] != null ? row[0].toString() : "-";
            Long count = ((Number) row[1]).longValue();
            summary.merge(modelName, count, Long::sum);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);

        return CommonResponse.success(data);
    }

    private Map<String, Object> toBiometricMap(BiometricData biometricData) {

        Map<String, Object> map = new LinkedHashMap<>();

        Device device = biometricData.getDevice();

        List<EmergencyRecord> emergencyRecords =
                emergencyRecordRepository.findAllByBiometricDataId(biometricData.getId());

        EmergencyRecord latestEmergencyRecord = emergencyRecords.stream()
                .filter(e -> e.getEmergencyRecordTime() != null)
                .max(Comparator.comparing(EmergencyRecord::getEmergencyRecordTime))
                .orElse(null);

        map.put("id", biometricData.getId());

        map.put("branchName", getBranchName(device));
        map.put("deviceId", device != null ? device.getId() : null);
        map.put("modelName", getModelName(device));
        map.put("battery", getBattery(device));

        map.put("userName", biometricData.getUserName());
        map.put("latestUseDate", biometricData.getLatestUseDate());
        map.put("latestUseTime", biometricData.getLatestUseTime());
        map.put("useTimePerDay", biometricData.getUseTimePerDay());
        map.put("breathPerDay", biometricData.getBreathPerDay());
        map.put("stepsPerDay", biometricData.getStepsPerDay());
        map.put("totalUseTime", biometricData.getTotalUseTime());
        map.put("totalSteps", biometricData.getTotalSteps());

        map.put("emergencyYn", latestEmergencyRecord != null ? "Y" : "N");
        map.put("emergencyRecordTime",
                latestEmergencyRecord != null ? latestEmergencyRecord.getEmergencyRecordTime() : null);

        map.put("latestUpdateTime", biometricData.getLatestUpdateTime());
        map.put("latestLocation", biometricData.getLatestLocation());

        return map;
    }

    private Map<String, Object> toEmergencyMap(EmergencyRecord emergencyRecord) {

        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", emergencyRecord.getId());
        map.put("biometricDataId",
                emergencyRecord.getBiometricData() != null
                        ? emergencyRecord.getBiometricData().getId()
                        : null);
        map.put("emergencyType", emergencyRecord.getEmergencyType());
        map.put("emergencyRecordTime", emergencyRecord.getEmergencyRecordTime());
        map.put("actionContent", emergencyRecord.getActionContent());
        map.put("actionResult", emergencyRecord.getActionResult());

        return map;
    }

    private String getModelName(Device device) {
        try {
            if (device == null ||
                    device.getModelVersion() == null ||
                    device.getModelVersion().getModel() == null) {
                return "-";
            }

            return device.getModelVersion().getModel().getModelName();
        } catch (Exception e) {
            return "-";
        }
    }

    private String getBranchName(Device device) {
        String branchName = firstNonBlank(
                callGetterAsString(device, "getBranchName"),
                callGetterAsString(device, "getCenterName"),
                callGetterAsString(device, "getLocationName")
        );

        if (branchName != null) {
            return branchName;
        }

        Object branch = firstNonNull(
                callGetter(device, "getBranch"),
                callGetter(device, "getCenter"),
                callGetter(device, "getRentalBranch")
        );

        return firstNonBlank(
                callGetterAsString(branch, "getBranchName"),
                callGetterAsString(branch, "getCenterName"),
                callGetterAsString(branch, "getName")
        );
    }

    private String getBattery(Device device) {
        String battery = firstNonBlank(
                callGetterAsString(device, "getBattery"),
                callGetterAsString(device, "getBatteryLevel"),
                callGetterAsString(device, "getBatteryPercent"),
                callGetterAsString(device, "getRemainBattery")
        );

        return battery != null ? battery : "-";
    }

    private Object callGetter(Object target, String methodName) {
        try {
            if (target == null) return null;

            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private String callGetterAsString(Object target, String methodName) {
        Object value = callGetter(target, methodName);
        return value == null ? null : value.toString();
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return "-";
    }

    private Long toLong(Object value) {
        if (value == null || value.toString().isBlank()) {
            throw new CustomException("INVALID_REQUEST", "필수 값이 누락되었습니다.");
        }

        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new CustomException("INVALID_REQUEST", "숫자 형식이 올바르지 않습니다: " + value);
        }
    }

    private Integer toInteger(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new CustomException("INVALID_REQUEST", "숫자 형식이 올바르지 않습니다: " + value);
        }
    }

    private String toString(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value.toString());
        } catch (DateTimeParseException e) {
            throw new CustomException("INVALID_REQUEST", "날짜 형식이 올바르지 않습니다(YYYY-MM-DD): " + value);
        }
    }
}