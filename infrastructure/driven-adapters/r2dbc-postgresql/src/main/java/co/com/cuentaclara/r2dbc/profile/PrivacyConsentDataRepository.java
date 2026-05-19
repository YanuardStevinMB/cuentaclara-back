package co.com.cuentaclara.r2dbc.profile;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PrivacyConsentDataRepository extends ReactiveCrudRepository<PrivacyConsentData, UUID> {

    @Query("SELECT * FROM privacy_consents WHERE user_id = :userId ORDER BY accepted_at DESC LIMIT 1")
    Mono<PrivacyConsentData> findLatestByUserId(UUID userId);
}
