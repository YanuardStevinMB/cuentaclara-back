package co.com.cuentaclara.api.profile.dto;

public record ProfileResponse(
        String id,
        String email,
        String fullName,
        String phone,
        String status,
        String systemRole,
        PreferenceResponse preferences
) {
    public record PreferenceResponse(String currency, String dateFormat) {}
}
