package co.com.cuentaclara.model.audit;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuditLog {
    private UUID id;
    private UUID userId;
    private String action;
    private String entityType;
    private UUID entityId;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
}
