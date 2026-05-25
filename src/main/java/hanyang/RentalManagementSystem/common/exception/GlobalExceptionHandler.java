package hanyang.RentalManagementSystem.common.exception;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.entity.SystemErrorLog;
import hanyang.RentalManagementSystem.common.repository.SystemErrorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SystemErrorLogRepository errorLogRepository;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResponse<Void>> handleCustomException(
            CustomException e, HttpServletRequest request) {
        log.warn("[{}] {} - {}", e.getCode(), e.getMessage(), request.getRequestURI());
        saveErrorLog(e.getCode(), e.getMessage(), request, e);
        return ResponseEntity.status(e.getHttpStatus())
                .body(CommonResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(
            Exception e, HttpServletRequest request) {
        if (e instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.error("NOT_FOUND", "페이지를 찾을 수 없습니다."));
        }
        log.error("Unhandled exception: {} - {}", e.getMessage(), request.getRequestURI(), e);
        saveErrorLog("INTERNAL_SERVER_ERROR", e.getMessage(), request, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }

    private void saveErrorLog(String code, String message, HttpServletRequest request, Exception e) {
        try {
            String stackTrace = getStackTrace(e);
            SystemErrorLog errorLog = SystemErrorLog.builder()
                    .errorCode(code)
                    .errorMessage(message)
                    .requestUrl(request.getRequestURI())
                    .requestMethod(request.getMethod())
                    .clientIp(request.getRemoteAddr())
                    .stackTrace(stackTrace)
                    .build();
            errorLogRepository.save(errorLog);
        } catch (Exception ex) {
            log.error("Failed to save error log", ex);
        }
    }

    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        String trace = sw.toString();
        return trace.length() > 3000 ? trace.substring(0, 3000) : trace;
    }
}