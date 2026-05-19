package co.com.cuentaclara.r2dbc.profile;

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
@Table("privacy_consents")
public class PrivacyConsentData implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID userId;
    private String policyVersion;
    private boolean accepted;
    private String ipAddress;
    private Instant acceptedAt;

    @Override
    @Transient
    public boolean isNew() {
        return true;
    }
}
