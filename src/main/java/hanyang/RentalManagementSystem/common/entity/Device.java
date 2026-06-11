package hanyang.RentalManagementSystem.common.entity;

import hanyang.RentalManagementSystem.common.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "device")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Device extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "device_id", length = 50, nullable = false, unique = true)
    private String deviceId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_version_id", nullable = false)
    private ModelVersion modelVersion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    // 교수님 피드백 #5: 상태를 문자열 대신 Enum (EnumType.STRING → 기존 컬럼/값 유지)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.INCOMING;
    @Column(length = 10)
    private String battery;
    @Column(name = "branch_send_date")
    private LocalDate branchSendDate;
    @Column(name = "incoming_date")
    private LocalDate incomingDate;
    @Column(name = "latest_rental_date")
    private LocalDate latestRentalDate;
    @Column(name = "latest_as_date")
    private LocalDate latestAsDate;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    @Column(columnDefinition = "TEXT")
    private String remark;
}
