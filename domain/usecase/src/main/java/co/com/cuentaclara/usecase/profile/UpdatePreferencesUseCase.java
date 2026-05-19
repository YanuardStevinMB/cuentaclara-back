package co.com.cuentaclara.usecase.profile;

import co.com.cuentaclara.model.user.UserPreference;
import co.com.cuentaclara.model.user.gateways.UserPreferenceGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class UpdatePreferencesUseCase {

    private final UserPreferenceGateway preferenceGateway;

    public Mono<UserPreference> execute(UUID userId, String currency, String dateFormat) {
        return preferenceGateway.findByUserId(userId)
                .map(existing -> existing.toBuilder()
                        .currency(currency)
                        .dateFormat(dateFormat)
                        .updatedAt(Instant.now())
                        .build())
                .switchIfEmpty(Mono.just(UserPreference.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .currency(currency)
                        .dateFormat(dateFormat)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()))
                .flatMap(preferenceGateway::save);
    }
}
