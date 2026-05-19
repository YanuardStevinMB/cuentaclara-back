package co.com.cuentaclara.model.movement.gateways;

import co.com.cuentaclara.model.movement.Movement;
import co.com.cuentaclara.model.movement.MovementFilter;
import co.com.cuentaclara.model.movement.MovementSummary;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface MovementGateway {
    Mono<Movement> save(Movement movement);
    Mono<Movement> findById(UUID id);
    Mono<List<Movement>> findByFilter(MovementFilter filter);
    Mono<Long> countByFilter(MovementFilter filter);
    Mono<Movement> update(Movement movement);
    Mono<Void> softDelete(UUID id, UUID userId);
    Mono<MovementSummary> getSummary(MovementFilter filter);
}
