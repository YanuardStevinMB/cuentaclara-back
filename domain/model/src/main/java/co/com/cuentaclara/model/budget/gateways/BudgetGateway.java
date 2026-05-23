package co.com.cuentaclara.model.budget.gateways;

import co.com.cuentaclara.model.budget.Budget;
import co.com.cuentaclara.model.budget.BudgetSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BudgetGateway {
    Mono<Budget> save(Budget budget);
    Mono<Budget> findById(UUID id);
    Flux<Budget> findByUserAndPeriod(UUID userId, int month, int year);
    Flux<Budget> findByUser(UUID userId);
    Mono<Boolean> existsActive(UUID userId, UUID categoryId, int month, int year);
    Mono<Budget> update(Budget budget);
    Mono<Void> toggleActive(UUID id, boolean active);
    Mono<Void> softDelete(UUID id);
    Mono<BudgetSummary> getSummary(UUID userId, int month, int year);
}
