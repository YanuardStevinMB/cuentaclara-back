package co.com.cuentaclara.model.movement;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MovementSummary {
    private final BigDecimal totalIncome;
    private final BigDecimal totalExpenses;
    private final BigDecimal balance;
}
