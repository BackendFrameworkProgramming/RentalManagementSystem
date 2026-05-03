package hanyang.RentalManagementSystem.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final String code;
    private final HttpStatus httpStatus;

    public CustomException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public CustomException(String code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }
}
