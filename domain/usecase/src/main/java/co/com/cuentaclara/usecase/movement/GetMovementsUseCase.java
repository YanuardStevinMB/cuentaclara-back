package co.com.cuentaclara.usecase.movement;

import co.com.cuentaclara.model.movement.MovementFilter;
import co.com.cuentaclara.model.movement.MovementPage;
import co.com.cuentaclara.model.movement.gateways.MovementGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetMovementsUseCase {
    private final MovementGateway movementGateway;

    public Mono<MovementPage> execute(MovementFilter filter) {
        return movementGateway.findByFilter(filter)
                .zipWith(movementGateway.countByFilter(filter))
                .map(tuple -> MovementPage.builder()
                        .content(tuple.getT1())
                        .page(filter.getPage())
                        .size(filter.getSize())
                        .totalElements(tuple.getT2())
                        .totalPages((int) Math.ceil((double) tuple.getT2() / filter.getSize()))
                        .build());
    }
}
