package co.com.cuentaclara.usecase.budget;

import co.com.cuentaclara.model.budget.Budget;
import co.com.cuentaclara.model.budget.BudgetSummary;
import co.com.cuentaclara.model.budget.gateways.BudgetGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetBudgetsUseCase {
    private final BudgetGateway budgetGateway;

    public Mono<List<Budget>> execute(UUID userId, int month, int year) {
        return budgetGateway.findByUserAndPeriod(userId, month, year).collectList();
    }

    public Mono<List<Budget>> getAll(UUID userId) {
        return budgetGateway.findByUser(userId).collectList();
    }

    public Mono<BudgetSummary> getSummary(UUID userId, int month, int year) {
        return budgetGateway.getSummary(userId, month, year);
    }
}
