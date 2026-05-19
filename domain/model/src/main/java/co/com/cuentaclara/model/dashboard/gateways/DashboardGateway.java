package co.com.cuentaclara.model.dashboard.gateways;

import co.com.cuentaclara.model.dashboard.DashboardAlert;
import co.com.cuentaclara.model.dashboard.ExpenseByCategory;
import co.com.cuentaclara.model.dashboard.MonthlyFlow;
import co.com.cuentaclara.model.dashboard.UpcomingPayment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface DashboardGateway {
    Mono<BigDecimal> getTotalIncome(UUID userId, LocalDate from, LocalDate to);
    Mono<BigDecimal> getTotalExpenses(UUID userId, LocalDate from, LocalDate to);
    Flux<MonthlyFlow> getMonthlyFlow(UUID userId, int months);
    Flux<ExpenseByCategory> getExpensesByCategory(UUID userId, LocalDate from, LocalDate to);
    Flux<UpcomingPayment> getUpcomingPayments(UUID userId, int limit);
    Flux<DashboardAlert> getAlerts(UUID userId);
}
