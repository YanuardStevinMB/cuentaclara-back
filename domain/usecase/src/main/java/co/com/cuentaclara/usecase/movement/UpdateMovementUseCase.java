package co.com.cuentaclara.usecase.movement;

import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import co.com.cuentaclara.model.movement.Movement;
import co.com.cuentaclara.model.movement.gateways.MovementGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class UpdateMovementUseCase {
    private final MovementGateway movementGateway;

    public Mono<Movement> execute(UUID movementId, UUID userId, Movement update) {
        return movementGateway.findById(movementId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR)))
                .flatMap(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        return Mono.error(new BusinessException(ErrorCode.VALIDATION_ERROR));
                    }
                    existing.setCategoryId(update.getCategoryId());
                    existing.setAmount(update.getAmount());
                    existing.setDescription(update.getDescription());
                    existing.setMovementDate(update.getMovementDate());
                    existing.setScope(update.getScope());
                    existing.setPaymentMethod(update.getPaymentMethod());
                    existing.setUpdatedAt(Instant.now());
                    return movementGateway.update(existing);
                });
    }
}
