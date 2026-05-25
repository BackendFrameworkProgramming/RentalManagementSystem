package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity @Table(name = "system_error_log")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemErrorLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "error_code", length = 50, nullable = false)
    private String errorCode;
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    @Column(name = "request_url", length = 500)
    private String requestUrl;
    @Column(name = "request_method", length = 10)
    private String requestMethod;
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;
    @Column(name = "client_ip", length = 50)
    private String clientIp;
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
