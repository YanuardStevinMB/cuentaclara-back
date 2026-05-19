package co.com.cuentaclara.usecase.profile;

import co.com.cuentaclara.model.audit.AuditLog;
import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.UserStatus;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class DeactivateAccountUseCase {

    private final UserGateway userGateway;
    private final AuditLogGateway auditLogGateway;

    public Mono<Void> execute(UUID userId, String confirmationText, String ipAddress) {
        if (!"ELIMINAR".equalsIgnoreCase(confirmationText)) {
            return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR));
        }

        return userGateway.findById(userId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    User deactivated = user.toBuilder()
                            .status(UserStatus.INACTIVE)
                            .deletedAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    return userGateway.save(deactivated);
                })
                .flatMap(user -> registerAudit(userId, "DEACTIVATE_ACCOUNT", ipAddress))
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
