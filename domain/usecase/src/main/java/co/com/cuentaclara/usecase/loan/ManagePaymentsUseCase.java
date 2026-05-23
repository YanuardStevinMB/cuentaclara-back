package co.com.cuentaclara.usecase.loan;

import co.com.cuentaclara.model.loan.LoanPayment;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class ManagePaymentsUseCase {
    private final LoanGateway loanGateway;

    public Mono<LoanPayment> registerPayment(LoanPayment payment) {
        payment.setId(UUID.randomUUID());
        payment.setStatus("ACTIVE");
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        return loanGateway.savePayment(payment);
    }

    public Flux<LoanPayment> getPayments(UUID loanId) {
        return loanGateway.getPayments(loanId);
    }

    public Mono<Void> voidPayment(UUID paymentId) {
        return loanGateway.voidPayment(paymentId);
    }
}
