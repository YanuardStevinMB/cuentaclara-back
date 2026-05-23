package co.com.cuentaclara.usecase.loan;

import co.com.cuentaclara.model.loan.LoanSummary;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class GetLoanSummaryUseCase {
    private final LoanGateway loanGateway;

    public Mono<LoanSummary> execute(UUID userId) {
        return loanGateway.getSummary(userId);
    }
}
