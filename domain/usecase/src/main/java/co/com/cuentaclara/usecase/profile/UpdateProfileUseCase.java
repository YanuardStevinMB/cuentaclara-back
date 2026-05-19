package co.com.cuentaclara.usecase.profile;

import co.com.cuentaclara.model.audit.AuditLog;
import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class UpdateProfileUseCase {

    private final UserGateway userGateway;
    private final AuditLogGateway auditLogGateway;

    public Mono<User> execute(UUID userId, String fullName, String phone, String ipAddress) {
        if (fullName == null || fullName.isBlank()) {
            return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR));
        }

        return userGateway.findById(userId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    User updated = user.toBuilder()
                            .fullName(fullName.trim())
                            .phone(phone)
                            .updatedAt(Instant.now())
                            .build();
                    return userGateway.save(updated);
                })
                .flatMap(user -> registerAudit(userId, "UPDATE_PROFILE", ipAddress).thenReturn(user));
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
