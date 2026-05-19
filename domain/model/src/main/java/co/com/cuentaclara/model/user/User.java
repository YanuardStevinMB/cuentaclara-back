package co.com.cuentaclara.model.user;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private String passwordHash;
    private SystemRole systemRole;
    private UserStatus status;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
