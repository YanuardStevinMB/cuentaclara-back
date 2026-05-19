package co.com.cuentaclara.usecase.dashboard;

import co.com.cuentaclara.model.dashboard.MonthlyFlow;
import co.com.cuentaclara.model.dashboard.gateways.DashboardGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
public class GetMonthlyFlowUseCase {

    private final DashboardGateway dashboardGateway;

    public Flux<MonthlyFlow> execute(UUID userId, int months) {
        return dashboardGateway.getMonthlyFlow(userId, months > 0 ? months : 6);
    }
}
