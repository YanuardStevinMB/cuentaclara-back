package co.com.cuentaclara.usecase.loan;

import co.com.cuentaclara.model.loan.Loan;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * UseCase to calculate and update loan status based on:
 * - Due date (OVERDUE if past and amount pending)
 * - Remaining amount (PAID if zero)
 * - Current status
 */
@RequiredArgsConstructor
public class CalculateLoanStatusUseCase {
    private final LoanGateway loanGateway;

    public Mono<String> calculateStatus(Loan loan) {
        // If already PAID or CANCELLED, keep status
        if ("PAID".equals(loan.getStatus()) || "CANCELLED".equals(loan.getStatus()) || "ARCHIVED".equals(loan.getStatus())) {
            return Mono.just(loan.getStatus());
        }

        BigDecimal remainingAmount = loan.getRemainingAmount() != null 
                ? loan.getRemainingAmount() 
                : loan.getExpectedTotalAmount().subtract(loan.getPaidAmount() != null ? loan.getPaidAmount() : BigDecimal.ZERO);

        // Check if loan is fully paid
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.just("PAID");
        }

        // Check if loan is overdue
        if (loan.getDueDate() != null && loan.getDueDate().isBefore(LocalDate.now())) {
            return Mono.just("OVERDUE");
        }

        return Mono.just("ACTIVE");
    }

    public Mono<Void> updateStatusIfNeeded(UUID loanId, Loan loan) {
        return calculateStatus(loan)
                .flatMap(calculatedStatus -> {
                    if (!calculatedStatus.equals(loan.getStatus())) {
                        return loanGateway.updateStatus(loanId, calculatedStatus);
                    }
                    return Mono.empty();
                });
    }
}
