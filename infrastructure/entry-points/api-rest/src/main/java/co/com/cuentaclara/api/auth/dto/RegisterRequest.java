package co.com.cuentaclara.api.auth.dto;

public record RegisterRequest(
        String fullName,
        String email,
        String password
) {}
