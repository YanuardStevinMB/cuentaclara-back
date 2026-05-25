package co.com.cuentaclara.usecase.loan;

import co.com.cuentaclara.model.loan.Loan;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@RequiredArgsConstructor
public class UpdateLoanUseCase {
    private final LoanGateway loanGateway;

    public Mono<Loan> execute(Loan loan) {
        return validate(loan)
                .flatMap(validatedLoan -> {
                    validatedLoan.setUpdatedAt(Instant.now());
                    // Recalculate expected total if interest was modified
                    calculateExpectedTotal(validatedLoan);
                    return loanGateway.update(validatedLoan);
                });
    }

    public Mono<Void> updateStatus(Loan loan, String newStatus) {
        return validateStatusTransition(loan.getStatus(), newStatus)
                .flatMap(valid -> loanGateway.updateStatus(loan.getId(), newStatus));
    }

    public Mono<Void> archive(Loan loan) {
        return loanGateway.updateStatus(loan.getId(), "ARCHIVED");
    }

    public Mono<Void> cancel(Loan loan) {
        return loanGateway.updateStatus(loan.getId(), "CANCELLED");
    }

    private Mono<Loan> validate(Loan loan) {
        // Don't allow editing if loan is PAID, CANCELLED or ARCHIVED
        if ("PAID".equals(loan.getStatus()) || "CANCELLED".equals(loan.getStatus()) || "ARCHIVED".equals(loan.getStatus())) {
            return Mono.error(new IllegalArgumentException("Cannot update a finalized loan"));
        }

        if (loan.getPrincipalAmount() != null && loan.getPrincipalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Principal amount must be greater than zero"));
        }

        return Mono.just(loan);
    }

    private Mono<Boolean> validateStatusTransition(String currentStatus, String newStatus) {
        // ACTIVE → OVERDUE, PAID, CANCELLED, ARCHIVED
        // OVERDUE → PAID, CANCELLED, ARCHIVED
        // PAID → ARCHIVED (read-only after paid)
        // CANCELLED → ARCHIVED
        // ARCHIVED → read-only

        if ("PAID".equals(currentStatus) || "ARCHIVED".equals(currentStatus)) {
            return Mono.error(new IllegalArgumentException("Cannot change status of finalized loan"));
        }

        return Mono.just(true);
    }

    private void calculateExpectedTotal(Loan loan) {
        if (loan.getInterestType() != null && loan.getInterestRate() != null) {
            BigDecimal expectedTotal = loan.getPrincipalAmount();

            if ("FIXED".equals(loan.getInterestType())) {
                expectedTotal = loan.getPrincipalAmount().add(loan.getInterestRate());
                loan.setInterestAmount(loan.getInterestRate());
            } else if ("PERCENTAGE".equals(loan.getInterestType())) {
                BigDecimal interest = loan.getPrincipalAmount()
                        .multiply(loan.getInterestRate())
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                expectedTotal = loan.getPrincipalAmount().add(interest);
                loan.setInterestAmount(interest);
            }

            loan.setExpectedTotalAmount(expectedTotal);
        }
    }
}
