package hanyang.RentalManagementSystem.common.entity;

import hanyang.RentalManagementSystem.common.enums.AsStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "as_record")
@NoArgsConstructor @AllArgsConstructor @Builder
public class AsRecord extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    @Getter @Setter
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    @Getter @Setter
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @Getter @Setter
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "as_type_id")
    @Getter @Setter
    private AsType asType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    @Getter @Setter
    private Vendor vendor;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private AsStatus status;

    @Column(name = "branch_send_date")
    @Getter @Setter
    private LocalDate branchSendDate;

    @Column(name = "receipt_date")
    @Getter @Setter
    private LocalDate receiptDate;

    @Column(name = "receipt_by", length = 50)
    @Getter @Setter
    private String receiptBy;

    @Column(name = "receipt_content", columnDefinition = "TEXT")
    @Getter @Setter
    private String receiptContent;

    @Column(name = "confirm_by", length = 50)
    @Getter @Setter
    private String confirmBy;

    @Column(name = "confirm_date")
    @Getter @Setter
    private LocalDate confirmDate;

    @Column(name = "confirm_result", columnDefinition = "TEXT")
    @Getter @Setter
    private String confirmResult;

    @Column(name = "collect_date")
    @Getter @Setter
    private LocalDate collectDate;

    @Column(name = "assigned_to", length = 50)
    @Getter @Setter
    private String assignedTo;

    @Column(name = "complete_date")
    @Getter @Setter
    private LocalDate completeDate;

    @Column(name = "repair_content", columnDefinition = "TEXT")
    @Getter @Setter
    private String repairContent;

    @Column(name = "resend_date")
    @Getter @Setter
    private LocalDate resendDate;

    @Column(name = "cost")
    @Getter @Setter
    private Integer cost;

    @Column(name = "is_deleted", nullable = false)
    @Getter @Setter
    @Builder.Default
    private Boolean isDeleted = false;

    // 💡 타 팀원의 컴파일 에러(DeviceAsHistoryResponse 등)를 막기 위한 문자열 기반 하위 호환용 메서드
    public String getStatus() {
        return this.status == null ? null : this.status.name();
    }

    public void setStatus(String status) {
        this.status = status == null ? null : AsStatus.fromString(status);
    }

    // 💡 규민님 도메인 내부 서비스에서 안전하게 사용할 Enum 전용 입출력 메서드
    public AsStatus getStatusEnum() {
        return this.status;
    }

    public void setStatusEnum(AsStatus status) {
        this.status = status;
    }
}