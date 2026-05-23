package co.com.cuentaclara.model.loan;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class LoanSummary {
    private final BigDecimal totalGiven;
    private final BigDecimal totalReceived;
    private final BigDecimal balance;
    private final int activeLoansCount;
    private final int overdueLoansCount;
}
