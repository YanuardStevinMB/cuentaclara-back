package co.com.cuentaclara.usecase.auth;

import co.com.cuentaclara.model.audit.AuditLog;
import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import co.com.cuentaclara.model.auth.gateways.AuthTokenGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class LogoutUseCase {

    private final AuthTokenGateway authTokenGateway;
    private final AuditLogGateway auditLogGateway;

    public Mono<Void> execute(UUID userId, String ipAddress, String userAgent) {
        return authTokenGateway.revokeAllUserTokens(userId)
                .then(registerAudit(userId, ipAddress, userAgent));
    }

    private Mono<Void> registerAudit(UUID userId, String ipAddress, String userAgent) {
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .action("LOGOUT")
                .entityType("USER")
                .entityId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .build();
        return auditLogGateway.save(log).then();
    }
}
