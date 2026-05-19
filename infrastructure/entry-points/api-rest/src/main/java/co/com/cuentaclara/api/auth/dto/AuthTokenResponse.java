package co.com.cuentaclara.api.auth.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType
) {}
