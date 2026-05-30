package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "branch")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Branch extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "branch_name", length = 100, nullable = false)
    private String branchName;
    @Column(nullable = false)
    @Builder.Default
    private Boolean status = true;
    @Column(length = 200)
    private String address;
    @Column(name = "address_detail", length = 200)
    private String addressDetail;
    @Column(name = "manager_name", length = 50)
    private String managerName;
    @Column(length = 20)
    private String phone;
    @Column(length = 20)
    private String fax;
    @Column(name = "applied_date")
    private LocalDate appliedDate;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
