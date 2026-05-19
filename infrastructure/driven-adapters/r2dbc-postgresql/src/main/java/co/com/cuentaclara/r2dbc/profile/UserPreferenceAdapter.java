package co.com.cuentaclara.r2dbc.profile;

import co.com.cuentaclara.model.user.UserPreference;
import co.com.cuentaclara.model.user.gateways.UserPreferenceGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserPreferenceAdapter implements UserPreferenceGateway {

    private final UserPreferenceDataRepository repository;

    @Override
    public Mono<UserPreference> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(this::toEntity);
    }

    @Override
    public Mono<UserPreference> save(UserPreference preference) {
        UserPreferenceData data = toData(preference);
        return repository.save(data).map(this::toEntity);
    }

    private UserPreferenceData toData(UserPreference p) {
        return UserPreferenceData.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .currency(p.getCurrency())
                .dateFormat(p.getDateFormat())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private UserPreference toEntity(UserPreferenceData d) {
        return UserPreference.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .currency(d.getCurrency())
                .dateFormat(d.getDateFormat())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
