package co.com.cuentaclara.model.loan.gateways;

import co.com.cuentaclara.model.loan.Loan;
import co.com.cuentaclara.model.loan.LoanFilter;
import co.com.cuentaclara.model.loan.LoanPayment;
import co.com.cuentaclara.model.loan.LoanSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LoanGateway {
    Mono<Loan> save(Loan loan);
    Mono<Loan> findById(UUID id);
    Flux<Loan> findByFilter(LoanFilter filter);
    Mono<Long> countByFilter(LoanFilter filter);
    Mono<Loan> update(Loan loan);
    Mono<Void> updateStatus(UUID id, String status);
    Mono<Void> softDelete(UUID id);
    Mono<LoanSummary> getSummary(UUID userId);
    // Payments
    Mono<LoanPayment> savePayment(LoanPayment payment);
    Flux<LoanPayment> getPayments(UUID loanId);
    Mono<Void> voidPayment(UUID paymentId);
}
