package co.com.cuentaclara.usecase.movement;

import co.com.cuentaclara.model.movement.MovementFilter;
import co.com.cuentaclara.model.movement.MovementSummary;
import co.com.cuentaclara.model.movement.gateways.MovementGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetMovementSummaryUseCase {
    private final MovementGateway movementGateway;

    public Mono<MovementSummary> execute(MovementFilter filter) {
        return movementGateway.getSummary(filter);
    }
}
