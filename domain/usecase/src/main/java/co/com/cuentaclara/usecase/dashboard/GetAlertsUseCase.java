package co.com.cuentaclara.usecase.dashboard;

import co.com.cuentaclara.model.dashboard.DashboardAlert;
import co.com.cuentaclara.model.dashboard.gateways.DashboardGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
public class GetAlertsUseCase {

    private final DashboardGateway dashboardGateway;

    public Flux<DashboardAlert> execute(UUID userId) {
        return dashboardGateway.getAlerts(userId);
    }
}
