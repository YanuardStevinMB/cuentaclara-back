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
public class MonthlyFlow {
    private String month;
    private BigDecimal income;
    private BigDecimal expenses;
}
