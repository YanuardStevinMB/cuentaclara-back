package co.com.cuentaclara.model.auth.gateways;

import co.com.cuentaclara.model.auth.PasswordResetToken;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PasswordResetGateway {
    Mono<PasswordResetToken> save(PasswordResetToken token);
    Mono<PasswordResetToken> findValidToken(UUID userId, String tokenHash);
    Mono<PasswordResetToken> markAsUsed(UUID tokenId);
}
