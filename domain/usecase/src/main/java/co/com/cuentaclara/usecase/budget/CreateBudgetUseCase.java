package co.com.cuentaclara.usecase.budget;

import co.com.cuentaclara.model.budget.Budget;
import co.com.cuentaclara.model.budget.gateways.BudgetGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateBudgetUseCase {
    private final BudgetGateway budgetGateway;

    public Mono<Budget> execute(Budget budget) {
        return budgetGateway.existsActive(budget.getUserId(), budget.getCategoryId(),
                budget.getPeriodMonth(), budget.getPeriodYear())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                "Ya existe un presupuesto activo para esta categoría y periodo"));
                    }
                    budget.setId(UUID.randomUUID());
                    budget.setActive(true);
                    budget.setCreatedAt(Instant.now());
                    budget.setUpdatedAt(Instant.now());
                    if (budget.getAlertThresholdPercent() <= 0) {
                        budget.setAlertThresholdPercent(80);
                    }
                    return budgetGateway.save(budget);
                });
    }
}
