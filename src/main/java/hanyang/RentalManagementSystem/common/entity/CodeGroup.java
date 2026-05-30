package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code_group")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeGroup extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_code", length = 50, nullable = false, unique = true)
    private String groupCode;

    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @Column(length = 200)
    private String description;

    @Column(name = "use_yn", nullable = false)
    @Builder.Default
    private Boolean useYn = true;

    @OneToMany(mappedBy = "codeGroup", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CodeDetail> codeDetails = new ArrayList<>();
}
