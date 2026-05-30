package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "biometric_data")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BiometricData extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    private Rental rental;
    @Column(name = "user_name", length = 50)
    private String userName;
    @Column(name = "latest_use_date")
    private LocalDate latestUseDate;
    @Column(name = "latest_use_time", length = 20)
    private String latestUseTime;
    @Column(name = "use_time_per_day", length = 20)
    private String useTimePerDay;
    @Column(name = "breath_per_day")
    private Integer breathPerDay;
    @Column(name = "steps_per_day")
    private Integer stepsPerDay;
    @Column(name = "total_use_time", length = 20)
    private String totalUseTime;
    @Column(name = "total_steps")
    private Integer totalSteps;
    @Column(name = "latest_update_time")
    private LocalDateTime latestUpdateTime;
    @Column(name = "latest_location", length = 200)
    private String latestLocation;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
