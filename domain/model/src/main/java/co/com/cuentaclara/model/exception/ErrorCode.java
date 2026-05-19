package co.com.cuentaclara.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    EMAIL_ALREADY_EXISTS("AUTH_001", "Email is already registered"),
    INVALID_CREDENTIALS("AUTH_002", "Invalid email or password"),
    USER_INACTIVE("AUTH_003", "User account is not active"),
    TOKEN_EXPIRED("AUTH_004", "Token has expired"),
    TOKEN_INVALID("AUTH_005", "Token is invalid"),
    PASSWORD_RESET_TOKEN_EXPIRED("AUTH_006", "Password reset token has expired"),
    PASSWORD_RESET_TOKEN_USED("AUTH_007", "Password reset token has already been used"),
    USER_NOT_FOUND("AUTH_008", "User not found"),
    WEAK_PASSWORD("AUTH_009", "Password does not meet minimum security requirements"),
    VALIDATION_ERROR("GEN_001", "Validation error");

    private final String code;
    private final String message;
}
