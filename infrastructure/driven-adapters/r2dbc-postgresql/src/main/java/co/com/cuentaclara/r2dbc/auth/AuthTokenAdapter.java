package co.com.cuentaclara.r2dbc.auth;

import co.com.cuentaclara.model.auth.RefreshToken;
import co.com.cuentaclara.model.auth.gateways.AuthTokenGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuthTokenAdapter implements AuthTokenGateway {

    private final RefreshTokenDataRepository repository;

    @Override
    public Mono<RefreshToken> saveRefreshToken(RefreshToken token) {
        RefreshTokenData data = RefreshTokenData.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .tokenHash(token.getTokenHash())
                .deviceInfo(token.getDeviceInfo())
                .expiresAt(token.getExpiresAt())
                .revokedAt(token.getRevokedAt())
                .createdAt(token.getCreatedAt())
                .build();
        return repository.save(data).map(this::toEntity);
    }

    @Override
    public Mono<RefreshToken> findActiveRefreshToken(UUID userId, String tokenHash) {
        return repository.findActiveToken(userId, tokenHash).map(this::toEntity);
    }

    @Override
    public Mono<Void> revokeAllUserTokens(UUID userId) {
        return repository.revokeAllByUserId(userId);
    }

    private RefreshToken toEntity(RefreshTokenData data) {
        return RefreshToken.builder()
                .id(data.getId())
                .userId(data.getUserId())
                .tokenHash(data.getTokenHash())
                .deviceInfo(data.getDeviceInfo())
                .expiresAt(data.getExpiresAt())
                .revokedAt(data.getRevokedAt())
                .createdAt(data.getCreatedAt())
                .build();
    }
}
