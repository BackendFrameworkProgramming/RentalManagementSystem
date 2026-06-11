package hanyang.RentalManagementSystem.eunhye.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.BiometricData;
import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.entity.EmergencyRecord;
import hanyang.RentalManagementSystem.common.entity.Rental;
import hanyang.RentalManagementSystem.common.enums.RentalStatus;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.BiometricDataRepository;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.EmergencyRecordRepository;
import hanyang.RentalManagementSystem.common.repository.RentalRepository;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricCreateRequest;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricDetailResponse;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricListResponse;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricModelSummaryResponse;
import hanyang.RentalManagementSystem.eunhye.dto.BiometricResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmergencyCreateRequest;
import hanyang.RentalManagementSystem.eunhye.dto.EmergencyListResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmergencyResponse;
import hanyang.RentalManagementSystem.eunhye.dto.EmergencyUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class BiometricService {

    private final BiometricDataRepository biometricDataRepository;
    private final EmergencyRecordRepository emergencyRecordRepository;
    private final RentalRepository rentalRepository;
    private final DeviceRepository deviceRepository;

    @Transactional(readOnly = true)
    public CommonResponse<BiometricListResponse> getBiometricDataList(int page, int size) {
        Page<BiometricData> biometricPage = biometricDataRepository.findAllByIsDeletedFalse(PageRequest.of(page - 1, size));
        BiometricListResponse data = BiometricListResponse.builder()
                .biometricData(biometricPage.getContent().stream().map(this::toBiometricResponse).toList())
                .build();
        return CommonResponse.success(data, Pagination.of(biometricPage));
    }

    @Transactional(readOnly = true)
    public CommonResponse<BiometricDetailResponse> getBiometricDataDetail(Long id) {
        BiometricData biometricData = biometricDataRepository.findById(id)
                .orElseThrow(() -> new CustomException("BIOMETRIC_NOT_FOUND", "생체정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        BiometricDetailResponse data = BiometricDetailResponse.builder()
                .biometricData(toBiometricResponse(biometricData))
                .emergencyRecords(emergencyRecordRepository.findAllByBiometricDataId(id).stream()
                        .map(EmergencyResponse::from).toList())
                .build();
        return CommonResponse.success(data);
    }

    public CommonResponse<BiometricResponse> createBiometricData(BiometricCreateRequest req) {
        if (req.getDeviceId() == null) {
            throw new CustomException("INVALID_REQUEST", "deviceId는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        Device device = deviceRepository.findById(req.getDeviceId())
                .orElseThrow(() -> new CustomException("DEVICE_NOT_FOUND", "디바이스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        Rental rental = rentalRepository.findByDeviceIdAndStatusAndIsDeletedFalse(req.getDeviceId(), RentalStatus.RENTING).orElse(null);

        BiometricData biometricData = BiometricData.builder()
                .device(device)
                .rental(rental)
                .userName(req.getUserName())
                .latestUseDate(parseDate(req.getLatestUseDate()))
                .latestUseTime(req.getLatestUseTime())
                .useTimePerDay(req.getUseTimePerDay())
                .breathPerDay(req.getBreathPerDay())
                .stepsPerDay(req.getStepsPerDay())
                .totalUseTime(req.getTotalUseTime())
                .totalSteps(req.getTotalSteps())
                .latestUpdateTime(LocalDateTime.now())
                .latestLocation(req.getLatestLocation())
                .isDeleted(false)
                .build();
        biometricDataRepository.save(biometricData);
        return CommonResponse.created(toBiometricResponse(biometricData));
    }

    public CommonResponse<Void> deleteBiometricData(Long id) {
        BiometricData biometricData = biometricDataRepository.findById(id)
                .orElseThrow(() -> new CustomException("BIOMETRIC_NOT_FOUND", "생체정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        biometricData.setIsDeleted(true);
        return CommonResponse.success(null);
    }

    @Transactional(readOnly = true)
    public CommonResponse<EmergencyListResponse> getEmergencyRecords(int page, int size) {
        Page<EmergencyRecord> emergencyPage = emergencyRecordRepository.findAll(PageRequest.of(page - 1, size));
        EmergencyListResponse data = EmergencyListResponse.builder()
                .emergencyRecords(emergencyPage.getContent().stream().map(EmergencyResponse::from).toList())
                .build();
        return CommonResponse.success(data, Pagination.of(emergencyPage));
    }

    public CommonResponse<EmergencyResponse> createEmergencyRecord(EmergencyCreateRequest req) {
        if (req.getBiometricDataId() == null) {
            throw new CustomException("INVALID_REQUEST", "biometricDataId는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        BiometricData biometricData = biometricDataRepository.findById(req.getBiometricDataId())
                .orElseThrow(() -> new CustomException("BIOMETRIC_NOT_FOUND", "생체정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        EmergencyRecord emergencyRecord = EmergencyRecord.builder()
                .biometricData(biometricData)
                .rental(biometricData.getRental())
                .emergencyType(req.getEmergencyType())
                .emergencyRecordTime(LocalDateTime.now())
                .actionContent(req.getActionContent())
                .actionResult(req.getActionResult())
                .build();
        emergencyRecordRepository.save(emergencyRecord);
        return CommonResponse.created(EmergencyResponse.from(emergencyRecord));
    }

    public CommonResponse<EmergencyResponse> updateEmergencyRecord(Long id, EmergencyUpdateRequest req) {
        EmergencyRecord emergencyRecord = emergencyRecordRepository.findById(id)
                .orElseThrow(() -> new CustomException("EMERGENCY_NOT_FOUND", "응급기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (req.getActionContent() != null) emergencyRecord.setActionContent(req.getActionContent());
        if (req.getActionResult() != null) emergencyRecord.setActionResult(req.getActionResult());
        return CommonResponse.success(EmergencyResponse.from(emergencyRecord));
    }

    @Transactional(readOnly = true)
    public CommonResponse<BiometricModelSummaryResponse> getSummaryByModel() {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (Object[] row : biometricDataRepository.countByModelName()) {
            String modelName = row[0] != null ? row[0].toString() : "-";
            Long count = ((Number) row[1]).longValue();
            summary.merge(modelName, count, Long::sum);
        }
        return CommonResponse.success(BiometricModelSummaryResponse.builder().summary(summary).build());
    }

    /** 생체정보 DTO 빌드 (최신 응급기록으로 emergencyYn 계산). */
    private BiometricResponse toBiometricResponse(BiometricData bd) {
        Device device = bd.getDevice();
        EmergencyRecord latest = emergencyRecordRepository.findAllByBiometricDataId(bd.getId()).stream()
                .filter(e -> e.getEmergencyRecordTime() != null)
                .max(Comparator.comparing(EmergencyRecord::getEmergencyRecordTime))
                .orElse(null);

        String modelName = (device != null && device.getModelVersion() != null && device.getModelVersion().getModel() != null)
                ? device.getModelVersion().getModel().getModelName() : "-";
        String branchName = (device != null && device.getBranch() != null && device.getBranch().getBranchName() != null)
                ? device.getBranch().getBranchName() : "-";
        String battery = (device != null && device.getBattery() != null) ? device.getBattery() : "-";

        return BiometricResponse.builder()
                .id(bd.getId())
                .branchName(branchName)
                .deviceId(device != null ? device.getId() : null)
                .modelName(modelName)
                .battery(battery)
                .userName(bd.getUserName())
                .latestUseDate(bd.getLatestUseDate())
                .latestUseTime(bd.getLatestUseTime())
                .useTimePerDay(bd.getUseTimePerDay())
                .breathPerDay(bd.getBreathPerDay())
                .stepsPerDay(bd.getStepsPerDay())
                .totalUseTime(bd.getTotalUseTime())
                .totalSteps(bd.getTotalSteps())
                .emergencyYn(latest != null ? "Y" : "N")
                .emergencyRecordTime(latest != null ? latest.getEmergencyRecordTime() : null)
                .latestUpdateTime(bd.getLatestUpdateTime())
                .latestLocation(bd.getLatestLocation())
                .build();
    }

    private LocalDate parseDate(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new CustomException("INVALID_REQUEST", "날짜 형식이 올바르지 않습니다(YYYY-MM-DD): " + v, HttpStatus.BAD_REQUEST);
        }
    }
}
