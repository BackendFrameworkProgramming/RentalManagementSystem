package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "refresh_token")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String token;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
