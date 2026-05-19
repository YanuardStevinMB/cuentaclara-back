package co.com.cuentaclara.r2dbc.profile;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserPreferenceDataRepository extends ReactiveCrudRepository<UserPreferenceData, UUID> {

    @Query("SELECT * FROM user_preferences WHERE user_id = :userId")
    Mono<UserPreferenceData> findByUserId(UUID userId);
}
