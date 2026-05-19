package co.com.cuentaclara.api.profile.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {}
