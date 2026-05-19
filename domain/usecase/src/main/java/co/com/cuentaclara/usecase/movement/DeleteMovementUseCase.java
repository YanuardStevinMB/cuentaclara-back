package co.com.cuentaclara.usecase.movement;

import co.com.cuentaclara.model.movement.gateways.MovementGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteMovementUseCase {
    private final MovementGateway movementGateway;

    public Mono<Void> execute(UUID movementId, UUID userId) {
        return movementGateway.softDelete(movementId, userId);
    }
}
