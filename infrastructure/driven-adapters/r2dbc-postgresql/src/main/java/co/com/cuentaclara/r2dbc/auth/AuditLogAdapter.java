package co.com.cuentaclara.r2dbc.auth;

import co.com.cuentaclara.model.audit.AuditLog;
import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class AuditLogAdapter implements AuditLogGateway {

    private final AuditLogDataRepository repository;

    @Override
    public Mono<AuditLog> save(AuditLog auditLog) {
        AuditLogData data = AuditLogData.builder()
                .id(auditLog.getId())
                .actorUserId(auditLog.getUserId())
                .action(auditLog.getAction())
                .entityName(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .createdAt(auditLog.getCreatedAt())
                .build();
        return repository.save(data).map(this::toEntity);
    }

    private AuditLog toEntity(AuditLogData data) {
        return AuditLog.builder()
                .id(data.getId())
                .userId(data.getActorUserId())
                .action(data.getAction())
                .entityType(data.getEntityName())
                .entityId(data.getEntityId())
                .ipAddress(data.getIpAddress())
                .userAgent(data.getUserAgent())
                .createdAt(data.getCreatedAt())
                .build();
    }
}
