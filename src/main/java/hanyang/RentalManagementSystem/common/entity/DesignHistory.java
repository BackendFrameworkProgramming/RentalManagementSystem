package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "design_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DesignHistory extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer round;
    @Column(name = "round_date", length = 30)
    private String roundDate;
    @Column(length = 50)
    private String source;
    @Column(name = "source_type", length = 20)
    private String sourceType;
    @Column(length = 200)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String changes;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
