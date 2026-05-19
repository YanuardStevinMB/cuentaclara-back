package co.com.cuentaclara.api.auth.dto;

public record LoginRequest(
        String email,
        String password
) {}
