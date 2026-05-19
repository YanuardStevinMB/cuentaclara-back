package co.com.cuentaclara.model.movement;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class MovementPage {
    private final List<Movement> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
}
