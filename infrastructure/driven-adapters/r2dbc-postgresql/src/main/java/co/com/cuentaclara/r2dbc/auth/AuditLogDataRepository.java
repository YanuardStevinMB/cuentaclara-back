package co.com.cuentaclara.r2dbc.auth;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface AuditLogDataRepository extends ReactiveCrudRepository<AuditLogData, UUID> {
}
