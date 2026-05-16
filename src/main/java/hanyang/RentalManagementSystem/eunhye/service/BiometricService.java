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

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        List<BiometricData> biometricDataList =
                biometricDataRepository.findAllByIsDeletedFalse(PageRequest.of(0, 9999)).getContent();

        Map<String, Integer> summary = new LinkedHashMap<>();

        for (BiometricData biometricData : biometricDataList) {

            String modelName =
                    biometricData.getDevice()
                            .getModelVersion()
                            .getModel()
                            .getModelName();

            summary.put(modelName,
                    summary.getOrDefault(modelName, 0) + 1);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", summary);

        return CommonResponse.success(data);
    }

    private Map<String, Object> toBiometricMap(BiometricData biometricData) {

        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", biometricData.getId());
        map.put("deviceId", biometricData.getDevice().getId());
        map.put("userName", biometricData.getUserName());
        map.put("latestUseDate", biometricData.getLatestUseDate());
        map.put("latestUseTime", biometricData.getLatestUseTime());
        map.put("useTimePerDay", biometricData.getUseTimePerDay());
        map.put("breathPerDay", biometricData.getBreathPerDay());
        map.put("stepsPerDay", biometricData.getStepsPerDay());
        map.put("totalUseTime", biometricData.getTotalUseTime());
        map.put("totalSteps", biometricData.getTotalSteps());
        map.put("latestUpdateTime", biometricData.getLatestUpdateTime());
        map.put("latestLocation", biometricData.getLatestLocation());

        return map;
    }

    private Map<String, Object> toEmergencyMap(EmergencyRecord emergencyRecord) {

        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", emergencyRecord.getId());
        map.put("biometricDataId", emergencyRecord.getBiometricData().getId());
        map.put("emergencyType", emergencyRecord.getEmergencyType());
        map.put("emergencyRecordTime", emergencyRecord.getEmergencyRecordTime());
        map.put("actionContent", emergencyRecord.getActionContent());
        map.put("actionResult", emergencyRecord.getActionResult());

        return map;
    }

    private Long toLong(Object value) {
        if (value == null) {
            throw new CustomException("INVALID_REQUEST", "필수 값이 누락되었습니다.");
        }
        return Long.valueOf(value.toString());
    }

    private Integer toInteger(Object value) {
        return value == null ? null : Integer.valueOf(value.toString());
    }

    private String toString(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDate toLocalDate(Object value) {
        return value == null || value.toString().isBlank()
                ? null
                : LocalDate.parse(value.toString());
    }
}