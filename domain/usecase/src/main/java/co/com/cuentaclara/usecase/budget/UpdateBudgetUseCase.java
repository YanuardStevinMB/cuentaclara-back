package co.com.cuentaclara.usecase.budget;

import co.com.cuentaclara.model.budget.Budget;
import co.com.cuentaclara.model.budget.gateways.BudgetGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
public class UpdateBudgetUseCase {
    private final BudgetGateway budgetGateway;

    public Mono<Budget> execute(Budget budget) {
        budget.setUpdatedAt(Instant.now());
        return budgetGateway.update(budget);
    }

    public Mono<Void> toggleStatus(java.util.UUID id, boolean active) {
        return budgetGateway.toggleActive(id, active);
    }
}
