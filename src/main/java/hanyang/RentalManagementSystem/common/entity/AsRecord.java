package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "as_record")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsRecord extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    private Rental rental;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "as_type_id")
    private AsType asType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;
    @Column(length = 20, nullable = false)
    private String status;
    @Column(name = "branch_send_date")
    private LocalDate branchSendDate;
    @Column(name = "receipt_date")
    private LocalDate receiptDate;
    @Column(name = "receipt_by", length = 50)
    private String receiptBy;
    @Column(name = "receipt_content", columnDefinition = "TEXT")
    private String receiptContent;
    @Column(name = "confirm_by", length = 50)
    private String confirmBy;
    @Column(name = "confirm_date")
    private LocalDate confirmDate;
    @Column(name = "confirm_result", columnDefinition = "TEXT")
    private String confirmResult;
    @Column(name = "collect_date")
    private LocalDate collectDate;
    @Column(name = "assigned_to", length = 50)
    private String assignedTo;
    @Column(name = "complete_date")
    private LocalDate completeDate;
    @Column(name = "resend_date")
    private LocalDate resendDate;
    @Column(name = "repair_content", columnDefinition = "TEXT")
    private String repairContent;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
