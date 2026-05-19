package co.com.cuentaclara.model.user.gateways;

import co.com.cuentaclara.model.user.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserGateway {
    Mono<User> save(User user);
    Mono<User> findByEmail(String email);
    Mono<User> findById(UUID id);
    Mono<User> updateLastLogin(UUID userId);
}
