package co.com.cuentaclara.usecase.dashboard;

import co.com.cuentaclara.model.dashboard.DashboardSummary;
import co.com.cuentaclara.model.dashboard.gateways.DashboardGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
public class GetDashboardSummaryUseCase {

    private final DashboardGateway dashboardGateway;

    public Mono<DashboardSummary> execute(UUID userId) {
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());

        Mono<BigDecimal> incomeMono = dashboardGateway.getTotalIncome(userId, firstDay, lastDay)
                .defaultIfEmpty(BigDecimal.ZERO);
        Mono<BigDecimal> expensesMono = dashboardGateway.getTotalExpenses(userId, firstDay, lastDay)
                .defaultIfEmpty(BigDecimal.ZERO);

        return Mono.zip(incomeMono, expensesMono)
                .map(tuple -> {
                    BigDecimal income = tuple.getT1();
                    BigDecimal expenses = tuple.getT2();
                    return DashboardSummary.builder()
                            .totalIncome(income)
                            .totalExpenses(expenses)
                            .balance(income.subtract(expenses))
                            .activeLoansGiven(0)
                            .activeLoansReceived(0)
                            .overdueDebts(0)
                            .build();
                });
    }
}
