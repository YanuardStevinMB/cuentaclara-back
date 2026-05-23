package co.com.cuentaclara.r2dbc.budget;

import co.com.cuentaclara.model.budget.Budget;
import co.com.cuentaclara.model.budget.BudgetSummary;
import co.com.cuentaclara.model.budget.gateways.BudgetGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BudgetAdapter implements BudgetGateway {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Budget> save(Budget budget) {
        return databaseClient.sql("""
                INSERT INTO budgets (id, user_id, category_id, period_month, period_year,
                    limit_amount, alert_threshold_percent, active, created_at, updated_at)
                VALUES (:id, :userId, :categoryId, :month, :year,
                    :limit, :threshold, :active, :createdAt, :updatedAt)
                """)
                .bind("id", budget.getId())
                .bind("userId", budget.getUserId())
                .bind("categoryId", budget.getCategoryId())
                .bind("month", budget.getPeriodMonth())
                .bind("year", budget.getPeriodYear())
                .bind("limit", budget.getLimitAmount())
                .bind("threshold", budget.getAlertThresholdPercent())
                .bind("active", budget.isActive())
                .bind("createdAt", budget.getCreatedAt())
                .bind("updatedAt", budget.getUpdatedAt())
                .then()
                .thenReturn(budget);
    }

    @Override
    public Mono<Budget> findById(UUID id) {
        return databaseClient.sql("""
                SELECT b.*, c.name as category_name, c.icon as category_icon, c.color as category_color,
                    COALESCE((SELECT SUM(m.amount) FROM movements m
                        WHERE m.category_id = b.category_id AND m.user_id = b.user_id
                        AND m.type = 'EXPENSE' AND m.deleted_at IS NULL
                        AND EXTRACT(MONTH FROM m.movement_date) = b.period_month
                        AND EXTRACT(YEAR FROM m.movement_date) = b.period_year), 0) as spent_amount
                FROM budgets b
                JOIN categories c ON c.id = b.category_id
                WHERE b.id = :id AND b.deleted_at IS NULL
                """)
                .bind("id", id)
                .map(this::mapBudgetRow)
                .one();
    }

    @Override
    public Flux<Budget> findByUserAndPeriod(UUID userId, int month, int year) {
        return databaseClient.sql("""
                SELECT b.*, c.name as category_name, c.icon as category_icon, c.color as category_color,
                    COALESCE((SELECT SUM(m.amount) FROM movements m
                        WHERE m.category_id = b.category_id AND m.user_id = b.user_id
                        AND m.type = 'EXPENSE' AND m.deleted_at IS NULL
                        AND EXTRACT(MONTH FROM m.movement_date) = b.period_month
                        AND EXTRACT(YEAR FROM m.movement_date) = b.period_year), 0) as spent_amount
                FROM budgets b
                JOIN categories c ON c.id = b.category_id
                WHERE b.user_id = :userId AND b.period_month = :month AND b.period_year = :year
                    AND b.deleted_at IS NULL
                ORDER BY b.active DESC, c.name ASC
                """)
                .bind("userId", userId)
                .bind("month", month)
                .bind("year", year)
                .map(this::mapBudgetRow)
                .all();
    }

    @Override
    public Flux<Budget> findByUser(UUID userId) {
        return databaseClient.sql("""
                SELECT b.*, c.name as category_name, c.icon as category_icon, c.color as category_color,
                    COALESCE((SELECT SUM(m.amount) FROM movements m
                        WHERE m.category_id = b.category_id AND m.user_id = b.user_id
                        AND m.type = 'EXPENSE' AND m.deleted_at IS NULL
                        AND EXTRACT(MONTH FROM m.movement_date) = b.period_month
                        AND EXTRACT(YEAR FROM m.movement_date) = b.period_year), 0) as spent_amount
                FROM budgets b
                JOIN categories c ON c.id = b.category_id
                WHERE b.user_id = :userId AND b.deleted_at IS NULL
                ORDER BY b.period_year DESC, b.period_month DESC, c.name ASC
                """)
                .bind("userId", userId)
                .map(this::mapBudgetRow)
                .all();
    }

    @Override
    public Mono<Boolean> existsActive(UUID userId, UUID categoryId, int month, int year) {
        return databaseClient.sql("""
                SELECT COUNT(*) as cnt FROM budgets
                WHERE user_id = :userId AND category_id = :categoryId
                    AND period_month = :month AND period_year = :year
                    AND active = TRUE AND deleted_at IS NULL
                """)
                .bind("userId", userId)
                .bind("categoryId", categoryId)
                .bind("month", month)
                .bind("year", year)
                .map(row -> row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<Budget> update(Budget budget) {
        return databaseClient.sql("""
                UPDATE budgets SET limit_amount = :limit, alert_threshold_percent = :threshold,
                    updated_at = :updatedAt
                WHERE id = :id AND deleted_at IS NULL
                """)
                .bind("id", budget.getId())
                .bind("limit", budget.getLimitAmount())
                .bind("threshold", budget.getAlertThresholdPercent())
                .bind("updatedAt", budget.getUpdatedAt())
                .then()
                .thenReturn(budget);
    }

    @Override
    public Mono<Void> toggleActive(UUID id, boolean active) {
        return databaseClient.sql("UPDATE budgets SET active = :active, updated_at = NOW() WHERE id = :id")
                .bind("id", id)
                .bind("active", active)
                .then();
    }

    @Override
    public Mono<Void> softDelete(UUID id) {
        return databaseClient.sql("UPDATE budgets SET deleted_at = NOW() WHERE id = :id")
                .bind("id", id)
                .then();
    }

    @Override
    public Mono<BudgetSummary> getSummary(UUID userId, int month, int year) {
        return databaseClient.sql("""
                SELECT
                    COALESCE(SUM(b.limit_amount), 0) as total_budgeted,
                    COUNT(*) as active_count
                FROM budgets b
                WHERE b.user_id = :userId AND b.period_month = :month AND b.period_year = :year
                    AND b.active = TRUE AND b.deleted_at IS NULL
                """)
                .bind("userId", userId)
                .bind("month", month)
                .bind("year", year)
                .map(row -> {
                    BigDecimal totalBudgeted = row.get("total_budgeted", BigDecimal.class);
                    int activeCount = row.get("active_count", Long.class).intValue();
                    return BudgetSummary.builder()
                            .totalBudgeted(totalBudgeted)
                            .activeBudgetsCount(activeCount)
                            .build();
                })
                .one()
                .flatMap(partial -> {
                    // Get total spent across all budgeted categories
                    return databaseClient.sql("""
                            SELECT
                                COALESCE(SUM(m.amount), 0) as total_spent
                            FROM movements m
                            WHERE m.user_id = :userId AND m.type = 'EXPENSE' AND m.deleted_at IS NULL
                                AND EXTRACT(MONTH FROM m.movement_date) = :month
                                AND EXTRACT(YEAR FROM m.movement_date) = :year
                                AND m.category_id IN (
                                    SELECT category_id FROM budgets
                                    WHERE user_id = :userId AND period_month = :month AND period_year = :year
                                        AND active = TRUE AND deleted_at IS NULL
                                )
                            """)
                            .bind("userId", userId)
                            .bind("month", month)
                            .bind("year", year)
                            .map(row -> row.get("total_spent", BigDecimal.class))
                            .one()
                            .map(totalSpent -> {
                                partial.setTotalSpent(totalSpent);
                                partial.setTotalRemaining(partial.getTotalBudgeted().subtract(totalSpent));
                                return partial;
                            });
                })
                .flatMap(partial -> {
                    // Count exceeded and near-limit budgets
                    return findByUserAndPeriod(userId, month, year)
                            .collectList()
                            .map(budgets -> {
                                int exceeded = 0;
                                int nearLimit = 0;
                                for (Budget b : budgets) {
                                    if (!b.isActive()) continue;
                                    if (b.getPercentUsed() >= 100) exceeded++;
                                    else if (b.getPercentUsed() >= b.getAlertThresholdPercent()) nearLimit++;
                                }
                                partial.setExceededCount(exceeded);
                                partial.setNearLimitCount(nearLimit);
                                return partial;
                            });
                });
    }

    private Budget mapBudgetRow(io.r2dbc.spi.Readable row) {
        BigDecimal limit = row.get("limit_amount", BigDecimal.class);
        BigDecimal spent = row.get("spent_amount", BigDecimal.class);
        int percent = limit.compareTo(BigDecimal.ZERO) > 0
                ? spent.multiply(BigDecimal.valueOf(100)).divide(limit, 0, RoundingMode.HALF_UP).intValue()
                : 0;

        return Budget.builder()
                .id(row.get("id", UUID.class))
                .userId(row.get("user_id", UUID.class))
                .categoryId(row.get("category_id", UUID.class))
                .periodMonth(row.get("period_month", Integer.class))
                .periodYear(row.get("period_year", Integer.class))
                .limitAmount(limit)
                .alertThresholdPercent(row.get("alert_threshold_percent", Integer.class))
                .active(row.get("active", Boolean.class))
                .createdAt(row.get("created_at", Instant.class))
                .updatedAt(row.get("updated_at", Instant.class))
                .categoryName(row.get("category_name", String.class))
                .categoryIcon(row.get("category_icon", String.class))
                .categoryColor(row.get("category_color", String.class))
                .spentAmount(spent)
                .remainingAmount(limit.subtract(spent))
                .percentUsed(percent)
                .build();
    }
}
