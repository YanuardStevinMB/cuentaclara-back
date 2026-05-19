package co.com.cuentaclara.usecase.profile;

import co.com.cuentaclara.model.audit.AuditLog;
import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import co.com.cuentaclara.model.auth.gateways.PasswordHashGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserGateway userGateway;
    private final PasswordHashGateway passwordHashGateway;
    private final AuditLogGateway auditLogGateway;

    private static final int MIN_PASSWORD_LENGTH = 8;

    public Mono<Void> execute(UUID userId, String currentPassword, String newPassword, String ipAddress) {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            return Mono.error(new BusinessException(ErrorCode.WEAK_PASSWORD));
        }

        return userGateway.findById(userId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    if (!passwordHashGateway.matches(currentPassword, user.getPasswordHash())) {
                        return Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS));
                    }
                    String newHash = passwordHashGateway.hash(newPassword);
                    User updated = user.toBuilder()
                            .passwordHash(newHash)
                            .updatedAt(Instant.now())
                            .build();
                    return userGateway.save(updated);
                })
                .flatMap(user -> registerAudit(userId, "CHANGE_PASSWORD", ipAddress))
                .then();
    }

    private Mono<AuditLog> registerAudit(UUID userId, String action, String ipAddress) {
        return auditLogGateway.save(AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .action(action)
                .entityType("USER")
                .entityId(userId)
                .ipAddress(ipAddress)
                .createdAt(Instant.now())
                .build());
    }
}
