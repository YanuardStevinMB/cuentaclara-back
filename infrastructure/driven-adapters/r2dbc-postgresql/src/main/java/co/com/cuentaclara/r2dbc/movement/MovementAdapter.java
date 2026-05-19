package co.com.cuentaclara.r2dbc.movement;

import co.com.cuentaclara.model.movement.*;
import co.com.cuentaclara.model.movement.gateways.MovementGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MovementAdapter implements MovementGateway {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Movement> save(Movement movement) {
        return databaseClient.sql("""
                INSERT INTO movements (id, user_id, category_id, type, amount, description, movement_date, scope, payment_method, created_at, updated_at)
                VALUES (:id, :userId, :categoryId, :type, :amount, :description, :movementDate, :scope, :paymentMethod, :createdAt, :updatedAt)
                """)
                .bind("id", movement.getId())
                .bind("userId", movement.getUserId())
                .bind("categoryId", movement.getCategoryId())
                .bind("type", movement.getType().name())
                .bind("amount", movement.getAmount())
                .bind("description", movement.getDescription() != null ? movement.getDescription() : "")
                .bind("movementDate", movement.getMovementDate())
                .bind("scope", movement.getScope().name())
                .bind("paymentMethod", movement.getPaymentMethod() != null ? movement.getPaymentMethod() : "")
                .bind("createdAt", movement.getCreatedAt())
                .bind("updatedAt", movement.getUpdatedAt())
                .then()
                .thenReturn(movement);
    }

    @Override
    public Mono<Movement> findById(UUID id) {
        return databaseClient.sql("""
                SELECT m.*, c.name as category_name, c.color as category_color, c.icon as category_icon
                FROM movements m
                LEFT JOIN categories c ON c.id = m.category_id
                WHERE m.id = :id AND m.deleted_at IS NULL
                """)
                .bind("id", id)
                .map(this::mapRow)
                .one();
    }

    @Override
    public Mono<List<Movement>> findByFilter(MovementFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT m.*, c.name as category_name, c.color as category_color, c.icon as category_icon
                FROM movements m
                LEFT JOIN categories c ON c.id = m.category_id
                WHERE m.user_id = :userId AND m.deleted_at IS NULL
                """);
        appendFilters(sql, filter);
        sql.append(" ORDER BY m.movement_date DESC, m.created_at DESC");
        sql.append(" LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("userId", filter.getUserId())
                .bind("limit", filter.getSize())
                .bind("offset", filter.getPage() * filter.getSize());

        spec = bindFilters(spec, filter);

        return spec.map(this::mapRow).all().collectList();
    }

    @Override
    public Mono<Long> countByFilter(MovementFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) as total
                FROM movements m
                WHERE m.user_id = :userId AND m.deleted_at IS NULL
                """);
        appendFilters(sql, filter);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("userId", filter.getUserId());

        spec = bindFilters(spec, filter);

        return spec.map(row -> row.get("total", Long.class)).one();
    }

    @Override
    public Mono<Movement> update(Movement movement) {
        return databaseClient.sql("""
                UPDATE movements SET category_id = :categoryId, amount = :amount, description = :description,
                  movement_date = :movementDate, scope = :scope, payment_method = :paymentMethod, updated_at = :updatedAt
                WHERE id = :id AND deleted_at IS NULL
                """)
                .bind("id", movement.getId())
                .bind("categoryId", movement.getCategoryId())
                .bind("amount", movement.getAmount())
                .bind("description", movement.getDescription() != null ? movement.getDescription() : "")
                .bind("movementDate", movement.getMovementDate())
                .bind("scope", movement.getScope().name())
                .bind("paymentMethod", movement.getPaymentMethod() != null ? movement.getPaymentMethod() : "")
                .bind("updatedAt", movement.getUpdatedAt())
                .then()
                .thenReturn(movement);
    }

    @Override
    public Mono<Void> softDelete(UUID id, UUID userId) {
        return databaseClient.sql("""
                UPDATE movements SET deleted_at = NOW() WHERE id = :id AND user_id = :userId AND deleted_at IS NULL
                """)
                .bind("id", id)
                .bind("userId", userId)
                .then();
    }

    @Override
    public Mono<MovementSummary> getSummary(MovementFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                  COALESCE(SUM(CASE WHEN m.type = 'INCOME' THEN m.amount END), 0) as total_income,
                  COALESCE(SUM(CASE WHEN m.type = 'EXPENSE' THEN m.amount END), 0) as total_expenses
                FROM movements m
                WHERE m.user_id = :userId AND m.deleted_at IS NULL
                """);
        appendFilters(sql, filter);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("userId", filter.getUserId());

        spec = bindFilters(spec, filter);

        return spec.map(row -> {
            BigDecimal income = row.get("total_income", BigDecimal.class);
            BigDecimal expenses = row.get("total_expenses", BigDecimal.class);
            return MovementSummary.builder()
                    .totalIncome(income)
                    .totalExpenses(expenses)
                    .balance(income.subtract(expenses))
                    .build();
        }).one();
    }

    private void appendFilters(StringBuilder sql, MovementFilter filter) {
        if (filter.getType() != null) {
            sql.append(" AND m.type = :type");
        }
        if (filter.getScope() != null) {
            sql.append(" AND m.scope = :scope");
        }
        if (filter.getCategoryId() != null) {
            sql.append(" AND m.category_id = :categoryId");
        }
        if (filter.getDateFrom() != null) {
            sql.append(" AND m.movement_date >= :dateFrom");
        }
        if (filter.getDateTo() != null) {
            sql.append(" AND m.movement_date <= :dateTo");
        }
    }

    private DatabaseClient.GenericExecuteSpec bindFilters(DatabaseClient.GenericExecuteSpec spec, MovementFilter filter) {
        if (filter.getType() != null) {
            spec = spec.bind("type", filter.getType().name());
        }
        if (filter.getScope() != null) {
            spec = spec.bind("scope", filter.getScope().name());
        }
        if (filter.getCategoryId() != null) {
            spec = spec.bind("categoryId", filter.getCategoryId());
        }
        if (filter.getDateFrom() != null) {
            spec = spec.bind("dateFrom", filter.getDateFrom());
        }
        if (filter.getDateTo() != null) {
            spec = spec.bind("dateTo", filter.getDateTo());
        }
        return spec;
    }

    private Movement mapRow(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata meta) {
        return Movement.builder()
                .id(row.get("id", UUID.class))
                .userId(row.get("user_id", UUID.class))
                .categoryId(row.get("category_id", UUID.class))
                .type(MovementType.valueOf(row.get("type", String.class)))
                .amount(row.get("amount", BigDecimal.class))
                .description(row.get("description", String.class))
                .movementDate(row.get("movement_date", LocalDate.class))
                .scope(FinancialScope.valueOf(row.get("scope", String.class)))
                .paymentMethod(row.get("payment_method", String.class))
                .createdAt(row.get("created_at", Instant.class))
                .updatedAt(row.get("updated_at", Instant.class))
                .categoryName(row.get("category_name", String.class))
                .categoryColor(row.get("category_color", String.class))
                .categoryIcon(row.get("category_icon", String.class))
                .build();
    }
}
