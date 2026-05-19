package co.com.cuentaclara.usecase.dashboard;

import co.com.cuentaclara.model.dashboard.ExpenseByCategory;
import co.com.cuentaclara.model.dashboard.gateways.DashboardGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
public class GetExpensesByCategoryUseCase {

    private final DashboardGateway dashboardGateway;

    public Flux<ExpenseByCategory> execute(UUID userId) {
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());
        return dashboardGateway.getExpensesByCategory(userId, firstDay, lastDay);
    }
}
