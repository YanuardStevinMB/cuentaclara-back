package co.com.cuentaclara.r2dbc.auth;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserDataRepository extends ReactiveCrudRepository<UserData, UUID> {

    @Query("SELECT * FROM users WHERE email = :email AND deleted_at IS NULL")
    Mono<UserData> findByEmailAndDeletedAtIsNull(String email);

    @Query("UPDATE users SET last_login_at = NOW(), updated_at = NOW() WHERE id = :id RETURNING *")
    Mono<UserData> updateLastLogin(UUID id);
}
