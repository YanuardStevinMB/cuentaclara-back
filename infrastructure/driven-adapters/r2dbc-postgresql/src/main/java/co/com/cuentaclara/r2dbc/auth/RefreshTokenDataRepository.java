package co.com.cuentaclara.r2dbc.auth;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefreshTokenDataRepository extends ReactiveCrudRepository<RefreshTokenData, UUID> {

    @Query("SELECT * FROM refresh_tokens WHERE user_id = :userId AND token_hash = :tokenHash AND revoked_at IS NULL AND expires_at > NOW()")
    Mono<RefreshTokenData> findActiveToken(UUID userId, String tokenHash);

    @Modifying
    @Query("UPDATE refresh_tokens SET revoked_at = NOW() WHERE user_id = :userId AND revoked_at IS NULL")
    Mono<Void> revokeAllByUserId(UUID userId);
}
