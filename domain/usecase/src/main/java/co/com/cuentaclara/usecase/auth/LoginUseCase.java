package co.com.cuentaclara.usecase.auth;

import co.com.cuentaclara.model.audit.AuditLog;
import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import co.com.cuentaclara.model.auth.AuthToken;
import co.com.cuentaclara.model.auth.RefreshToken;
import co.com.cuentaclara.model.auth.gateways.AuthTokenGateway;
import co.com.cuentaclara.model.auth.gateways.JwtGateway;
import co.com.cuentaclara.model.auth.gateways.PasswordHashGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.UserStatus;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RequiredArgsConstructor
public class LoginUseCase {

    private final UserGateway userGateway;
    private final PasswordHashGateway passwordHashGateway;
    private final JwtGateway jwtGateway;
    private final AuthTokenGateway authTokenGateway;
    private final AuditLogGateway auditLogGateway;

    public Mono<AuthToken> execute(String email, String rawPassword, String ipAddress, String userAgent) {
        return userGateway.findByEmail(email.toLowerCase().trim())
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS)))
                .flatMap(user -> validateUser(user, rawPassword))
                .flatMap(user -> generateTokens(user, ipAddress, userAgent));
    }

    private Mono<User> validateUser(User user, String rawPassword) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            return Mono.error(new BusinessException(ErrorCode.USER_INACTIVE));
        }
        if (!passwordHashGateway.matches(rawPassword, user.getPasswordHash())) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        }
        return Mono.just(user);
    }

    private Mono<AuthToken> generateTokens(User user, String ipAddress, String userAgent) {
        String accessToken = jwtGateway.generateAccessToken(user);
        String refreshTokenValue = jwtGateway.generateRefreshToken(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash(refreshTokenValue)
                .deviceInfo(userAgent)
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .build();

        AuthToken authToken = AuthToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .expiresIn(jwtGateway.getAccessTokenExpirationMs())
                .tokenType("Bearer")
                .build();

        return authTokenGateway.saveRefreshToken(refreshToken)
                .then(userGateway.updateLastLogin(user.getId()))
                .then(registerAudit(user.getId(), "LOGIN", ipAddress, userAgent))
                .thenReturn(authToken);
    }

    private Mono<AuditLog> registerAudit(UUID userId, String action, String ipAddress, String userAgent) {
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .action(action)
                .entityType("USER")
                .entityId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .build();
        return auditLogGateway.save(log);
    }
}
