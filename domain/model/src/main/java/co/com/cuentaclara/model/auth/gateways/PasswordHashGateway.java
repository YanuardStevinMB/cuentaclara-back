package co.com.cuentaclara.model.auth.gateways;

public interface PasswordHashGateway {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
