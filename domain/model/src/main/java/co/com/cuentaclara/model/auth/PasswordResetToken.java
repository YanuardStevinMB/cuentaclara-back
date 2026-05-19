package co.com.cuentaclara.model.auth;

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
public class PasswordResetToken {
    private UUID id;
    private UUID userId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant createdAt;
}
