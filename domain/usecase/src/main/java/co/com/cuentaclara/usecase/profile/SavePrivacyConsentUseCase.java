package co.com.cuentaclara.usecase.profile;

import co.com.cuentaclara.model.user.PrivacyConsent;
import co.com.cuentaclara.model.user.gateways.PrivacyConsentGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class SavePrivacyConsentUseCase {

    private final PrivacyConsentGateway consentGateway;

    public Mono<PrivacyConsent> execute(UUID userId, String policyVersion, boolean accepted, String ipAddress) {
        PrivacyConsent consent = PrivacyConsent.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .policyVersion(policyVersion)
                .accepted(accepted)
                .ipAddress(ipAddress)
                .acceptedAt(Instant.now())
                .build();
        return consentGateway.save(consent);
    }
}
