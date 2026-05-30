package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "branch_manager")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchManager extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    @Column(name = "manager_name", length = 50, nullable = false)
    private String managerName;
    @Column(length = 20)
    private String contact;
    @Column(length = 100)
    private String email;
    @Column(name = "manager_type", length = 10, nullable = false)
    private String managerType;
    @Column(nullable = false)
    @Builder.Default
    private Boolean status = true;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
