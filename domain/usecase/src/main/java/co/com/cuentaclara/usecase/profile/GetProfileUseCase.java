package co.com.cuentaclara.usecase.profile;

import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.UserPreference;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import co.com.cuentaclara.model.user.gateways.UserPreferenceGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class GetProfileUseCase {

    private final UserGateway userGateway;
    private final UserPreferenceGateway preferenceGateway;

    public Mono<ProfileResult> execute(UUID userId) {
        Mono<User> userMono = userGateway.findById(userId);
        Mono<UserPreference> prefMono = preferenceGateway.findByUserId(userId)
                .defaultIfEmpty(UserPreference.builder()
                        .userId(userId)
                        .currency("COP")
                        .dateFormat("DD/MM/YYYY")
                        .build());

        return Mono.zip(userMono, prefMono)
                .map(tuple -> new ProfileResult(tuple.getT1(), tuple.getT2()));
    }

    public record ProfileResult(User user, UserPreference preference) {}
}
