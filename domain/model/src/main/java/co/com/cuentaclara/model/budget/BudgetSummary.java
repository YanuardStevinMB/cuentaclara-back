package co.com.cuentaclara.model.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BudgetSummary {
    private BigDecimal totalBudgeted;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;
    private int activeBudgetsCount;
    private int exceededCount;
    private int nearLimitCount;
}
