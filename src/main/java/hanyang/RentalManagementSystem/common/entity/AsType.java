package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "as_type")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsType extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "type_name", length = 50, nullable = false)
    private String typeName;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "use_yn", nullable = false)
    private Boolean useYn = true;
}
