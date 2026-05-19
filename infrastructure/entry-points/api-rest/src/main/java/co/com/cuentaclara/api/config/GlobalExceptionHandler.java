package co.com.cuentaclara.api.config;

import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = resolveHttpStatus(errorCode);

        Map<String, Object> body = Map.of(
                "code", errorCode.getCode(),
                "message", errorCode.getMessage(),
                "timestamp", Instant.now().toString()
        );

        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus resolveHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_CREDENTIALS, USER_INACTIVE -> HttpStatus.UNAUTHORIZED;
            case TOKEN_EXPIRED, TOKEN_INVALID, PASSWORD_RESET_TOKEN_EXPIRED, PASSWORD_RESET_TOKEN_USED -> HttpStatus.UNAUTHORIZED;
            case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case WEAK_PASSWORD, VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
        };
    }
}
