package co.com.cuentaclara.r2dbc.profile;

import co.com.cuentaclara.model.user.PrivacyConsent;
import co.com.cuentaclara.model.user.gateways.PrivacyConsentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PrivacyConsentAdapter implements PrivacyConsentGateway {

    private final PrivacyConsentDataRepository repository;

    @Override
    public Mono<PrivacyConsent> findLatestByUserId(UUID userId) {
        return repository.findLatestByUserId(userId).map(this::toEntity);
    }

    @Override
    public Mono<PrivacyConsent> save(PrivacyConsent consent) {
        PrivacyConsentData data = toData(consent);
        return repository.save(data).map(this::toEntity);
    }

    private PrivacyConsentData toData(PrivacyConsent c) {
        return PrivacyConsentData.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .policyVersion(c.getPolicyVersion())
                .accepted(c.isAccepted())
                .ipAddress(c.getIpAddress())
                .acceptedAt(c.getAcceptedAt())
                .build();
    }

    private PrivacyConsent toEntity(PrivacyConsentData d) {
        return PrivacyConsent.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .policyVersion(d.getPolicyVersion())
                .accepted(d.isAccepted())
                .ipAddress(d.getIpAddress())
                .acceptedAt(d.getAcceptedAt())
                .build();
    }
}
