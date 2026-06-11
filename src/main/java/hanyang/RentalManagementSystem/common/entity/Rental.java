package hanyang.RentalManagementSystem.common.entity;

import hanyang.RentalManagementSystem.common.enums.RentalStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "rental")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rental extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // 교수님 피드백 #5: 상태를 문자열 대신 Enum (EnumType.STRING → 기존 컬럼/값 유지)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RentalStatus status;
    @Column(name = "apply_date")
    private LocalDate applyDate;
    @Column(name = "use_start_date")
    private LocalDate useStartDate;
    @Column(name = "return_due_date")
    private LocalDate returnDueDate;
    @Column(name = "receive_date")
    private LocalDate receiveDate;
    @Column(name = "wear_yn")
    private Boolean wearYn;
    @Column(name = "return_date")
    private LocalDate returnDate;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
