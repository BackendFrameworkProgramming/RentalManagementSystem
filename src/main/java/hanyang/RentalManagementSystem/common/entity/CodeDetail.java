package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "code_detail")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeDetail extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_group_id", nullable = false)
    private CodeGroup codeGroup;

    @Column(name = "code_value", length = 50, nullable = false)
    private String codeValue;

    @Column(name = "code_name", length = 100, nullable = false)
    private String codeName;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "use_yn", nullable = false)
    private Boolean useYn = true;
}
