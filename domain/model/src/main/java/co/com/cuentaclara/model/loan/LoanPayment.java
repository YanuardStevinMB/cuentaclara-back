package co.com.cuentaclara.model.loan;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanPayment {
    private UUID id;
    private UUID loanId;
    private UUID userId;
    private BigDecimal amount;
    private LocalDate paidOn;
    private String paymentMethod;
    private String description;
    private String status; // ACTIVE, VOIDED
    private Instant createdAt;
    private Instant updatedAt;
}
