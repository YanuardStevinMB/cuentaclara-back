package co.com.cuentaclara.model.audit.gateways;

import co.com.cuentaclara.model.audit.AuditLog;
import reactor.core.publisher.Mono;

public interface AuditLogGateway {
    Mono<AuditLog> save(AuditLog auditLog);
}
