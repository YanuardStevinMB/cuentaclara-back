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
@Table("user_preferences")
public class UserPreferenceData implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID userId;
    private String currency;
    private String dateFormat;
    private Instant createdAt;
    private Instant updatedAt;

    @Override
    @Transient
    public boolean isNew() {
        return createdAt != null && createdAt.equals(updatedAt);
    }
}
