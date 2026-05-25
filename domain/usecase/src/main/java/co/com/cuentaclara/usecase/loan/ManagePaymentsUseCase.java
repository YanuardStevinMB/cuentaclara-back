package co.com.cuentaclara.usecase.loan;

import co.com.cuentaclara.model.loan.Loan;
import co.com.cuentaclara.model.loan.LoanPayment;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class ManagePaymentsUseCase {
    private final LoanGateway loanGateway;

    public Mono<LoanPayment> registerPayment(LoanPayment payment) {
        return loanGateway.findById(payment.getLoanId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Loan not found")))
                .flatMap(loan -> validatePayment(payment, loan))
                .flatMap(validPayment -> {
                    validPayment.setId(UUID.randomUUID());
                    validPayment.setStatus("ACTIVE");
                    validPayment.setCreatedAt(Instant.now());
                    validPayment.setUpdatedAt(Instant.now());
                    return loanGateway.savePayment(validPayment)
                            .flatMap(savedPayment -> checkAndUpdateLoanStatus(loan, payment)
                                    .thenReturn(savedPayment));
                });
    }

    public Flux<LoanPayment> getPayments(UUID loanId) {
        return loanGateway.getPayments(loanId);
    }

    public Mono<Void> voidPayment(UUID paymentId) {
        return loanGateway.voidPayment(paymentId);
    }

    private Mono<LoanPayment> validatePayment(LoanPayment payment, Loan loan) {
        // Cannot add payments to PAID, CANCELLED or ARCHIVED loans
        if ("PAID".equals(loan.getStatus()) || "CANCELLED".equals(loan.getStatus()) || "ARCHIVED".equals(loan.getStatus())) {
            return Mono.error(new IllegalArgumentException("Cannot add payments to finalized loan"));
        }

        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Payment amount must be greater than zero"));
        }

        return Mono.just(payment);
    }

    private Mono<Void> checkAndUpdateLoanStatus(Loan loan, LoanPayment payment) {
        BigDecimal remainingAmount = loan.getRemainingAmount() != null 
                ? loan.getRemainingAmount() 
                : loan.getExpectedTotalAmount().subtract(loan.getPaidAmount() != null ? loan.getPaidAmount() : BigDecimal.ZERO);

        BigDecimal newRemaining = remainingAmount.subtract(payment.getAmount());

        // If remaining amount is zero or less, mark loan as PAID
        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            return loanGateway.updateStatus(loan.getId(), "PAID");
        }

        return Mono.empty();
    }
}
