package co.com.cuentaclara.r2dbc.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("audit_logs")
public class AuditLogData implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID tenantId;
    private UUID actorUserId;
    private String action;
    private String entityName;
    private UUID entityId;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;

    @Override
    @Transient
    public boolean isNew() {
        return true;
    }
}
