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
@Table("users")
public class UserData implements Persistable<UUID> {
    @Id
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private String passwordHash;
    private String systemRole;
    private String status;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    @Override
    @Transient
    public boolean isNew() {
        return createdAt == null || createdAt.equals(updatedAt);
    }
}
