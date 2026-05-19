package co.com.cuentaclara.model.movement;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class MovementFilter {
    private final UUID userId;
    private final MovementType type;
    private final FinancialScope scope;
    private final UUID categoryId;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final int page;
    private final int size;
}
