package co.com.cuentaclara.model.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseByCategory {
    private String category;
    private String color;
    private BigDecimal amount;
    private double percentage;
}
