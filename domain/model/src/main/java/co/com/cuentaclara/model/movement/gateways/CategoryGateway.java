package co.com.cuentaclara.model.movement.gateways;

import co.com.cuentaclara.model.movement.Category;
import co.com.cuentaclara.model.movement.MovementType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CategoryGateway {
    Mono<Category> save(Category category);
    Mono<Category> findById(UUID id);
    Flux<Category> findByUserId(UUID userId);
    Flux<Category> findActiveByUserIdAndType(UUID userId, MovementType type);
    Mono<Category> update(Category category);
    Mono<Void> toggleStatus(UUID id, UUID userId, boolean active);
}
