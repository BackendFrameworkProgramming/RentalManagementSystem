package hanyang.RentalManagementSystem.common.entity;

import hanyang.RentalManagementSystem.common.enums.Role;
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
    // 교수님 피드백 #5: 역할을 문자열 대신 Enum으로 (EnumType.STRING → 기존 컬럼/값 유지)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Role role = Role.USER;
    @Column(length = 20)
    private String contact;
    @Column(length = 100)
    private String email;
    // 역할 모델: STAFF→직원, BRANCH_MANAGER→지점 연결 (nullable → 라이브 DB 컬럼 추가 안전)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
