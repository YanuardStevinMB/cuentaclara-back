package co.com.cuentaclara.model.movement;

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
public class Movement {
    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private MovementType type;
    private BigDecimal amount;
    private String description;
    private LocalDate movementDate;
    private FinancialScope scope;
    private String paymentMethod;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    // Transient (populated by joins)
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
}
