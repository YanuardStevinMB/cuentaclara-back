package co.com.cuentaclara.model.auth.gateways;

import co.com.cuentaclara.model.auth.RefreshToken;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AuthTokenGateway {
    Mono<RefreshToken> saveRefreshToken(RefreshToken token);
    Mono<RefreshToken> findActiveRefreshToken(UUID userId, String tokenHash);
    Mono<Void> revokeAllUserTokens(UUID userId);
}
