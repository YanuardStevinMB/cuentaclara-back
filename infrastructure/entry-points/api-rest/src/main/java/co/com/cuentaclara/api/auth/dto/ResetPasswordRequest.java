package co.com.cuentaclara.api.auth.dto;

public record ResetPasswordRequest(
        String token,
        String newPassword
) {}
