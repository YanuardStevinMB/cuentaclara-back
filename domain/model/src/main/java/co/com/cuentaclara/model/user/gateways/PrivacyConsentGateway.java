package co.com.cuentaclara.model.user.gateways;

import co.com.cuentaclara.model.user.PrivacyConsent;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PrivacyConsentGateway {
    Mono<PrivacyConsent> findLatestByUserId(UUID userId);
    Mono<PrivacyConsent> save(PrivacyConsent consent);
}
