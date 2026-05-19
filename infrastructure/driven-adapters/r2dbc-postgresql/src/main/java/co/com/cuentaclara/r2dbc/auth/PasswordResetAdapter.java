package co.com.cuentaclara.r2dbc.auth;

import co.com.cuentaclara.model.auth.PasswordResetToken;
import co.com.cuentaclara.model.auth.gateways.PasswordResetGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PasswordResetAdapter implements PasswordResetGateway {

    private final PasswordResetTokenDataRepository repository;

    @Override
    public Mono<PasswordResetToken> save(PasswordResetToken token) {
        PasswordResetTokenData data = PasswordResetTokenData.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .tokenHash(token.getTokenHash())
                .expiresAt(token.getExpiresAt())
                .usedAt(token.getUsedAt())
                .createdAt(token.getCreatedAt())
                .build();
        return repository.save(data).map(this::toEntity);
    }

    @Override
    public Mono<PasswordResetToken> findValidToken(UUID userId, String tokenHash) {
        return repository.findValidByTokenHash(tokenHash).map(this::toEntity);
    }

    @Override
    public Mono<PasswordResetToken> markAsUsed(UUID tokenId) {
        return repository.findById(tokenId)
                .flatMap(data -> {
                    data.setUsedAt(Instant.now());
                    return repository.save(data);
                })
                .map(this::toEntity);
    }

    private PasswordResetToken toEntity(PasswordResetTokenData data) {
        return PasswordResetToken.builder()
                .id(data.getId())
                .userId(data.getUserId())
                .tokenHash(data.getTokenHash())
                .expiresAt(data.getExpiresAt())
                .usedAt(data.getUsedAt())
                .createdAt(data.getCreatedAt())
                .build();
    }
}
