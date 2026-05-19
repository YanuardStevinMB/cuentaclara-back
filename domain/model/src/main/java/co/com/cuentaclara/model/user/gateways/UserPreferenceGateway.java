package co.com.cuentaclara.model.user.gateways;

import co.com.cuentaclara.model.user.UserPreference;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserPreferenceGateway {
    Mono<UserPreference> findByUserId(UUID userId);
    Mono<UserPreference> save(UserPreference preference);
}
