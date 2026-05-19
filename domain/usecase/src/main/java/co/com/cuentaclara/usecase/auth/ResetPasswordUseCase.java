package co.com.cuentaclara.usecase.auth;

import co.com.cuentaclara.model.audit.AuditLog;
import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import co.com.cuentaclara.model.auth.gateways.PasswordHashGateway;
import co.com.cuentaclara.model.auth.gateways.PasswordResetGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private final PasswordResetGateway passwordResetGateway;
    private final UserGateway userGateway;
    private final PasswordHashGateway passwordHashGateway;
    private final AuditLogGateway auditLogGateway;

    private static final int MIN_PASSWORD_LENGTH = 8;

    public Mono<Void> execute(String token, String newPassword, String ipAddress, String userAgent) {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            return Mono.error(new BusinessException(ErrorCode.WEAK_PASSWORD));
        }

        return findAndValidateToken(token)
                .flatMap(resetToken -> updatePassword(resetToken.getUserId(), newPassword)
                        .then(passwordResetGateway.markAsUsed(resetToken.getId()))
                        .then(registerAudit(resetToken.getUserId(), ipAddress, userAgent)));
    }

    private Mono<co.com.cuentaclara.model.auth.PasswordResetToken> findAndValidateToken(String token) {
        return passwordResetGateway.findValidToken(null, token)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.TOKEN_INVALID)))
                .flatMap(resetToken -> {
                    if (resetToken.getUsedAt() != null) {
                        return Mono.error(new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_USED));
                    }
                    if (resetToken.getExpiresAt().isBefore(Instant.now())) {
                        return Mono.error(new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED));
                    }
                    return Mono.just(resetToken);
                });
    }

    private Mono<User> updatePassword(UUID userId, String newPassword) {
        return userGateway.findById(userId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    String hashedPassword = passwordHashGateway.hash(newPassword);
                    User updatedUser = user.toBuilder()
                            .passwordHash(hashedPassword)
                            .updatedAt(Instant.now())
                            .build();
                    return userGateway.save(updatedUser);
                });
    }

    private Mono<Void> registerAudit(UUID userId, String ipAddress, String userAgent) {
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .action("PASSWORD_RESET")
                .entityType("USER")
                .entityId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .build();
        return auditLogGateway.save(log).then();
    }
}
