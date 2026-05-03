package hanyang.RentalManagementSystem.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorInfo {
    private String code;
    private String message;
    private List<BatchErrorDetail> details;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BatchErrorDetail {
        private Long targetId;
        private String reason;
    }
}
