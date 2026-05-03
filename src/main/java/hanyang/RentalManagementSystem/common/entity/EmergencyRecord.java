package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity @Table(name = "emergency_record")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biometric_data_id", nullable = false)
    private BiometricData biometricData;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    private Rental rental;
    @Column(name = "emergency_type", length = 50, nullable = false)
    private String emergencyType;
    @Column(name = "emergency_record_time", nullable = false)
    private LocalDateTime emergencyRecordTime;
    @Column(name = "action_content", columnDefinition = "TEXT")
    private String actionContent;
    @Column(name = "action_result", length = 200)
    private String actionResult;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
