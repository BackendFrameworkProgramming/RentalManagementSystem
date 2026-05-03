package hanyang.RentalManagementSystem.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {
    private boolean success;
    private T data;
    private Pagination pagination;
    private ErrorInfo error;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder().success(true).data(data).build();
    }

    public static <T> CommonResponse<T> success(T data, Pagination pagination) {
        return CommonResponse.<T>builder().success(true).data(data).pagination(pagination).build();
    }

    public static <T> CommonResponse<T> created(T data) {
        return CommonResponse.<T>builder().success(true).data(data).build();
    }

    public static <T> CommonResponse<T> error(String code, String message) {
        return CommonResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder().code(code).message(message).build())
                .build();
    }
}
