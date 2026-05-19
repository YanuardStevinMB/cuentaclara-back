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
public class PrivacyConsent {
    private UUID id;
    private UUID userId;
    private String policyVersion;
    private boolean accepted;
    private String ipAddress;
    private Instant acceptedAt;
}
