package co.com.cuentaclara.r2dbc.auth;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PasswordResetTokenDataRepository extends ReactiveCrudRepository<PasswordResetTokenData, UUID> {

    @Query("SELECT * FROM password_reset_tokens WHERE token_hash = :tokenHash AND used_at IS NULL AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    Mono<PasswordResetTokenData> findValidByTokenHash(String tokenHash);
}
