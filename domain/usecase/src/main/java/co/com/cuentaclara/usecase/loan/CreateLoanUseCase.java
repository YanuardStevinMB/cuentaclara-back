package co.com.cuentaclara.usecase.loan;

import co.com.cuentaclara.model.loan.Loan;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateLoanUseCase {
    private final LoanGateway loanGateway;

    public Mono<Loan> execute(Loan loan) {
        loan.setId(UUID.randomUUID());
        loan.setStatus("ACTIVE");
        loan.setCreatedAt(Instant.now());
        loan.setUpdatedAt(Instant.now());
        if (loan.getExpectedTotalAmount() == null) {
            loan.setExpectedTotalAmount(loan.getPrincipalAmount());
        }
        return loanGateway.save(loan);
    }
}
