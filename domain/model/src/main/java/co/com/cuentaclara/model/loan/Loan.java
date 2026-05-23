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
public class Loan {
    private UUID id;
    private UUID contactId;
    private UUID userId;
    private String loanDirection; // GIVEN, RECEIVED
    private String status;        // ACTIVE, OVERDUE, PAID, CANCELLED
    private String title;
    private BigDecimal principalAmount;
    private String interestType;  // NONE, FIXED, PERCENTAGE
    private BigDecimal interestRate;
    private BigDecimal expectedTotalAmount;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    // Transient
    private String contactName;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
}
