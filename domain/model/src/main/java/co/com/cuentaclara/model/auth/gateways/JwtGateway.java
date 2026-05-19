package co.com.cuentaclara.model.auth.gateways;

import co.com.cuentaclara.model.user.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface JwtGateway {
    String generateAccessToken(User user);
    String generateRefreshToken(UUID userId);
    Mono<UUID> validateAccessToken(String token);
    Mono<UUID> validateRefreshToken(String token);
    long getAccessTokenExpirationMs();
}
