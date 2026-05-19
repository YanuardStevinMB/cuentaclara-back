package co.com.cuentaclara.model.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingPayment {
    private UUID id;
    private String name;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String status;
    private String category;
}
