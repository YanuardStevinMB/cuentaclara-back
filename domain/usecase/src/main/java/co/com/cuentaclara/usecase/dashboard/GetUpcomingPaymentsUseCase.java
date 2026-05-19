package co.com.cuentaclara.usecase.dashboard;

import co.com.cuentaclara.model.dashboard.UpcomingPayment;
import co.com.cuentaclara.model.dashboard.gateways.DashboardGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
public class GetUpcomingPaymentsUseCase {

    private final DashboardGateway dashboardGateway;

    public Flux<UpcomingPayment> execute(UUID userId) {
        return dashboardGateway.getUpcomingPayments(userId, 5);
    }
}
