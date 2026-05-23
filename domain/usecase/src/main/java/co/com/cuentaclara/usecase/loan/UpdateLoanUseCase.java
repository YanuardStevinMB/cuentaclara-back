package co.com.cuentaclara.usecase.loan;

import co.com.cuentaclara.model.loan.Loan;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
public class UpdateLoanUseCase {
    private final LoanGateway loanGateway;

    public Mono<Loan> execute(Loan loan) {
        loan.setUpdatedAt(Instant.now());
        return loanGateway.update(loan);
    }

    public Mono<Void> updateStatus(Loan loan, String newStatus) {
        return loanGateway.updateStatus(loan.getId(), newStatus);
    }

    public Mono<Void> cancel(Loan loan) {
        return loanGateway.updateStatus(loan.getId(), "CANCELLED");
    }
}
