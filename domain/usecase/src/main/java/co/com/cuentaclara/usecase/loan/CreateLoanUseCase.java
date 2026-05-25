package co.com.cuentaclara.usecase.loan;

import co.com.cuentaclara.model.loan.Loan;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateLoanUseCase {
    private final LoanGateway loanGateway;

    public Mono<Loan> execute(Loan loan) {
        return validate(loan)
                .flatMap(validatedLoan -> {
                    validatedLoan.setId(UUID.randomUUID());
                    validatedLoan.setStatus("ACTIVE");
                    validatedLoan.setCreatedAt(Instant.now());
                    validatedLoan.setUpdatedAt(Instant.now());

                    // Calculate expected total amount with interest
                    calculateExpectedTotal(validatedLoan);

                    return loanGateway.save(validatedLoan);
                });
    }

    private Mono<Loan> validate(Loan loan) {
        if (loan.getPrincipalAmount() == null || loan.getPrincipalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Principal amount must be greater than zero"));
        }
        if (loan.getContactId() == null) {
            return Mono.error(new IllegalArgumentException("Contact ID is required"));
        }
        if (loan.getUserId() == null) {
            return Mono.error(new IllegalArgumentException("User ID is required"));
        }
        if (loan.getLoanDate() == null) {
            return Mono.error(new IllegalArgumentException("Loan date is required"));
        }
        if (loan.getDueDate() != null && loan.getDueDate().isBefore(loan.getLoanDate())) {
            return Mono.error(new IllegalArgumentException("Due date cannot be before loan date"));
        }

        // Validate interest
        if (loan.getInterestType() != null && !loan.getInterestType().equals("NONE")) {
            if (loan.getInterestRate() == null || loan.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
                return Mono.error(new IllegalArgumentException("Interest rate must be valid when interest type is set"));
            }
        }

        return Mono.just(loan);
    }

    private void calculateExpectedTotal(Loan loan) {
        BigDecimal expectedTotal = loan.getPrincipalAmount();

        if (loan.getInterestType() != null && loan.getInterestRate() != null) {
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
        }

        loan.setExpectedTotalAmount(expectedTotal);
    }
}
