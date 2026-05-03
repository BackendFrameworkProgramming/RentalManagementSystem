package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "vendor")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vendor extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "vendor_name", length = 100, nullable = false)
    private String vendorName;
    @Column(length = 20)
    private String contact;
    @Column(length = 200)
    private String address;
    @Column(length = 20, nullable = false)
    private String status = "ACTIVE";
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
