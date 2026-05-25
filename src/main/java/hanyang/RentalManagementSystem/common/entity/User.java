package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;
    @Column(name = "user_login_id", length = 50, unique = true)
    private String userLoginId;
    @Column(length = 255)
    private String password;
    @Column(length = 20)
    @Builder.Default
    private String role = "USER";
    @Column(length = 20)
    private String contact;
    @Column(length = 100)
    private String email;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
