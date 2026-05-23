package co.com.cuentaclara.model.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Budget {
    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private int periodMonth;
    private int periodYear;
    private BigDecimal limitAmount;
    private int alertThresholdPercent;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    // Transient / computed
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private int percentUsed;
}
