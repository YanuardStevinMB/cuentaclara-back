package co.com.cuentaclara.model.loan;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoanFilter {
    private final java.util.UUID userId;
    private final String direction; // GIVEN, RECEIVED, null=all
    private final String status;    // ACTIVE, OVERDUE, PAID, CANCELLED, null=all
    private final int page;
    private final int size;
}
