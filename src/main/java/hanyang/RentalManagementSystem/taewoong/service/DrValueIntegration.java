package hanyang.RentalManagementSystem.taewoong.service;

import com.drvalue.client.SihunClient;
import com.drvalue.client.model.EmergencyMessage;
import com.drvalue.client.model.PoweronMessage;
import com.drvalue.client.model.ReportMessage;
import hanyang.RentalManagementSystem.common.entity.BiometricData;
import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.entity.EmergencyRecord;
import hanyang.RentalManagementSystem.common.repository.BiometricDataRepository;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.EmergencyRecordRepository;
import hanyang.RentalManagementSystem.common.repository.RentalRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrValueIntegration {

    private final SihunClient client;
    private final DeviceRepository deviceRepository;
    private final BiometricDataRepository biometricDataRepository;
    private final EmergencyRecordRepository emergencyRecordRepository;
    private final RentalRepository rentalRepository;

    public final AtomicLong reportCount = new AtomicLong();
    public final AtomicLong poweronCount = new AtomicLong();
    public final AtomicLong emergencyCount = new AtomicLong();

    @PostConstruct
    public void wire() {
        client.onReport(this::handleReport);
        client.onPoweron(this::handlePoweron);
        client.onEmergency(this::handleEmergency);
        log.info("✓ SihunClient 핸들러 등록 완료");
    }

    private void handleReport(ReportMessage r) {
        long n = reportCount.incrementAndGet();
        log.info("REPORT #{} {} batt={}% step={} breath={}",
                n, r.deviceId(),
                String.format("%.1f", r.battery()),
                r.stepCount(), r.breathRate());

        // DB 저장
        try {
            saveReport(r);
        } catch (Exception e) {
            log.error("REPORT DB 저장 실패: {}", e.getMessage());
        }
    }

    private void handlePoweron(PoweronMessage p) {
        long n = poweronCount.incrementAndGet();
        log.info("POWERON #{} {} model={} fw={}", n, p.deviceId(), p.modelName(), p.firmwareVersion());
    }

    private void handleEmergency(EmergencyMessage e) {
        long n = emergencyCount.incrementAndGet();
        log.warn("🚨 EMERGENCY #{} {} kind={} ({}, {})",
                n, e.deviceId(),
                e.emergencyLabel(),
                String.format("%.4f", e.gpsLatitude()),
                String.format("%.4f", e.gpsLongitude()));

        // DB 저장
        try {
            saveEmergency(e);
        } catch (Exception ex) {
            log.error("EMERGENCY DB 저장 실패: {}", ex.getMessage());
        }
    }

    public void saveReport(ReportMessage r) {
        // deviceId(문자열)로 Device 엔티티 조회
        Optional<Device> deviceOpt = deviceRepository.findAll().stream()
                .filter(d -> d.getDeviceId().equals(r.deviceId()) && !d.getIsDeleted())
                .findFirst();

        if (deviceOpt.isEmpty()) {
            log.debug("REPORT 저장 스킵 - 미등록 디바이스: {}", r.deviceId());
            return;
        }

        Device device = deviceOpt.get();

        // 기존 생체정보 있으면 업데이트, 없으면 신규 생성
        Optional<BiometricData> existing = biometricDataRepository
                .findAllByDeviceIdAndIsDeletedFalse(device.getId())
                .stream().findFirst();

        BiometricData bio;
        if (existing.isPresent()) {
            bio = existing.get();
        } else {
            bio = new BiometricData();
            bio.setDevice(device);
            bio.setIsDeleted(false);
            // 현재 진행 중 임대가 있으면 rental_id 자동 세팅
            rentalRepository.findByDeviceIdAndStatusAndIsDeletedFalse(device.getId(), "RENTING")
                    .ifPresent(bio::setRental);
        }

        bio.setUserName(r.deviceId()); // 디바이스ID를 임시 사용자명으로
        bio.setLatestUseDate(LocalDate.now());
        bio.setLatestUseTime(LocalDateTime.now().toLocalTime().toString().substring(0, 8));
        bio.setStepsPerDay((int) r.stepCount());
        bio.setBreathPerDay((int) r.breathRate());
        bio.setLatestUpdateTime(LocalDateTime.now());
        bio.setLatestLocation(String.format("%.4f, %.4f", r.gpsLatitude(), r.gpsLongitude()));

        biometricDataRepository.save(bio);
    }

    public void saveEmergency(EmergencyMessage e) {
        // deviceId(문자열)로 Device 엔티티 조회
        Optional<Device> deviceOpt = deviceRepository.findAll().stream()
                .filter(d -> d.getDeviceId().equals(e.deviceId()) && !d.getIsDeleted())
                .findFirst();

        if (deviceOpt.isEmpty()) {
            log.debug("EMERGENCY 저장 스킵 - 미등록 디바이스: {}", e.deviceId());
            return;
        }

        Device device = deviceOpt.get();

        // 해당 디바이스의 생체정보 조회 (없으면 생성)
        BiometricData bio = biometricDataRepository
                .findAllByDeviceIdAndIsDeletedFalse(device.getId())
                .stream().findFirst()
                .orElseGet(() -> {
                    BiometricData newBio = new BiometricData();
                    newBio.setDevice(device);
                    newBio.setIsDeleted(false);
                    return biometricDataRepository.save(newBio);
                });

        EmergencyRecord record = new EmergencyRecord();
        record.setBiometricData(bio);
        record.setEmergencyType(e.emergencyLabel());
        record.setEmergencyRecordTime(LocalDateTime.now());
        record.setActionContent(String.format("위치: %.4f, %.4f", e.gpsLatitude(), e.gpsLongitude()));

        // 현재 진행 중 임대가 있으면 rental_id 세팅
        rentalRepository.findByDeviceIdAndStatusAndIsDeletedFalse(device.getId(), "RENTING")
                .ifPresent(record::setRental);

        emergencyRecordRepository.save(record);
    }
}