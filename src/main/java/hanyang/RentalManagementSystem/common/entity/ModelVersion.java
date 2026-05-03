package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "model_version")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModelVersion extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;
    @Column(length = 50, nullable = false)
    private String version;
    @Column(columnDefinition = "TEXT")
    private String spec;
    @Column(name = "manual_file_name", length = 200)
    private String manualFileName;
    @Column(name = "manual_path", columnDefinition = "TEXT")
    private String manualPath;
    @Column(name = "release_date")
    private LocalDate releaseDate;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
