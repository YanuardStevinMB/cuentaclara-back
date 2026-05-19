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
public class RefreshToken {
    private UUID id;
    private UUID userId;
    private String tokenHash;
    private String deviceInfo;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant createdAt;
}
