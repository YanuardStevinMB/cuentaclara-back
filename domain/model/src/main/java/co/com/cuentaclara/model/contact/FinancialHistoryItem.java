package co.com.cuentaclara.model.contact;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FinancialHistoryItem {
    private UUID loanId;
    private String title;
    private String direction; // GIVEN or RECEIVED
    private String status;    // ACTIVE, PAID, OVERDUE, CANCELLED
    private BigDecimal principalAmount;
    private BigDecimal paidAmount;
    private LocalDate loanDate;
    private LocalDate dueDate;
}
