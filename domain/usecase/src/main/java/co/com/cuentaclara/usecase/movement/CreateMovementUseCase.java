package co.com.cuentaclara.usecase.movement;

import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import co.com.cuentaclara.model.movement.Category;
import co.com.cuentaclara.model.movement.Movement;
import co.com.cuentaclara.model.movement.gateways.CategoryGateway;
import co.com.cuentaclara.model.movement.gateways.MovementGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateMovementUseCase {
    private final MovementGateway movementGateway;
    private final CategoryGateway categoryGateway;

    public Mono<Movement> execute(Movement movement) {
        return categoryGateway.findById(movement.getCategoryId())
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR)))
                .flatMap(category -> {
                    if (!category.isActive()) {
                        return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR));
                    }
                    movement.setId(UUID.randomUUID());
                    movement.setCreatedAt(Instant.now());
                    movement.setUpdatedAt(Instant.now());
                    return movementGateway.save(movement);
                });
    }
}
