package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "model")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Model extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "model_name", length = 100, nullable = false)
    private String modelName;
    @Column(length = 100)
    private String manufacturer;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    @OneToMany(mappedBy = "model", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ModelVersion> versions = new ArrayList<>();
}
