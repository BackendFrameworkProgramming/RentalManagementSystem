package hanyang.RentalManagementSystem.taewoong.service;

import com.drvalue.client.SihunClient;
import com.drvalue.client.model.EmergencyMessage;
import com.drvalue.client.model.PoweronMessage;
import com.drvalue.client.model.ReportMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrValueIntegration {

    private final SihunClient client;

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
    }

    private void handlePoweron(PoweronMessage p) {
        long n = poweronCount.incrementAndGet();
        log.info("POWERON #{} {} model={}", n, p.deviceId(), p.modelName());
    }

    private void handleEmergency(EmergencyMessage e) {
        long n = emergencyCount.incrementAndGet();
        log.warn("🚨 EMERGENCY #{} {} kind={}", n, e.deviceId(), e.emergencyLabel());
    }
}