package hanyang.RentalManagementSystem.taewoong.controller;

import com.drvalue.client.DrValueException;
import com.drvalue.client.SihunClient;
import com.drvalue.client.model.DeviceInfo;
import hanyang.RentalManagementSystem.taewoong.service.DrValueIntegration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
public class MqttController {

    private final SihunClient client;
    private final DrValueIntegration integration;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        try {
            String deviceId = (String) body.get("deviceId");
            String modelName = body.get("modelName") != null ? (String) body.get("modelName") : "WF100";
            int intervalMs = body.get("intervalMs") != null ? ((Number) body.get("intervalMs")).intValue() : 5000;
            DeviceInfo info = client.registerDevice(deviceId, modelName, intervalMs);
            return ResponseEntity.status(201).body(info);
        } catch (DrValueException e) {
            return ResponseEntity.status(e.statusCode() > 0 ? e.statusCode() : 500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/unregister/{deviceId}")
    public ResponseEntity<?> unregister(@PathVariable String deviceId) {
        try {
            client.unregisterDevice(deviceId);
            return ResponseEntity.ok(Map.of("deviceId", deviceId, "status", "unregistered"));
        } catch (DrValueException e) {
            return ResponseEntity.status(e.statusCode() > 0 ? e.statusCode() : 500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/devices")
    public List<DeviceInfo> list() {
        return client.listDevices();
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
                "report", integration.reportCount.get(),
                "poweron", integration.poweronCount.get(),
                "emergency", integration.emergencyCount.get()
        );
    }
}