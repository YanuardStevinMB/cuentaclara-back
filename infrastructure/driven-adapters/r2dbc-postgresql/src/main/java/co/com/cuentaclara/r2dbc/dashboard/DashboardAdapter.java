package co.com.cuentaclara.r2dbc.dashboard;

import co.com.cuentaclara.model.dashboard.DashboardAlert;
import co.com.cuentaclara.model.dashboard.ExpenseByCategory;
import co.com.cuentaclara.model.dashboard.MonthlyFlow;
import co.com.cuentaclara.model.dashboard.UpcomingPayment;
import co.com.cuentaclara.model.dashboard.gateways.DashboardGateway;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DashboardAdapter implements DashboardGateway {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<BigDecimal> getTotalIncome(UUID userId, LocalDate from, LocalDate to) {
        return databaseClient.sql("""
                SELECT COALESCE(SUM(amount), 0) as total
                FROM movements
                WHERE user_id = :userId AND type = 'INCOME'
                  AND movement_date >= :from AND movement_date <= :to
                  AND deleted_at IS NULL
                """)
                .bind("userId", userId)
                .bind("from", from)
                .bind("to", to)
                .map(row -> row.get("total", BigDecimal.class))
                .one();
    }

    @Override
    public Mono<BigDecimal> getTotalExpenses(UUID userId, LocalDate from, LocalDate to) {
        return databaseClient.sql("""
                SELECT COALESCE(SUM(amount), 0) as total
                FROM movements
                WHERE user_id = :userId AND type = 'EXPENSE'
                  AND movement_date >= :from AND movement_date <= :to
                  AND deleted_at IS NULL
                """)
                .bind("userId", userId)
                .bind("from", from)
                .bind("to", to)
                .map(row -> row.get("total", BigDecimal.class))
                .one();
    }

    @Override
    public Flux<MonthlyFlow> getMonthlyFlow(UUID userId, int months) {
        return databaseClient.sql("""
                WITH months AS (
                    SELECT generate_series(
                        date_trunc('month', CURRENT_DATE) - INTERVAL '1 month' * (:months - 1),
                        date_trunc('month', CURRENT_DATE),
                        INTERVAL '1 month'
                    )::date AS month_start
                )
                SELECT
                    TO_CHAR(m.month_start, 'Mon') as month,
                    COALESCE(SUM(CASE WHEN mv.type = 'INCOME' THEN mv.amount END), 0) as income,
                    COALESCE(SUM(CASE WHEN mv.type = 'EXPENSE' THEN mv.amount END), 0) as expenses
                FROM months m
                LEFT JOIN movements mv ON mv.user_id = :userId
                    AND mv.movement_date >= m.month_start
                    AND mv.movement_date < m.month_start + INTERVAL '1 month'
                    AND mv.deleted_at IS NULL
                GROUP BY m.month_start
                ORDER BY m.month_start
                """)
                .bind("userId", userId)
                .bind("months", months)
                .map(row -> MonthlyFlow.builder()
                        .month(row.get("month", String.class))
                        .income(row.get("income", BigDecimal.class))
                        .expenses(row.get("expenses", BigDecimal.class))
                        .build())
                .all();
    }

    @Override
    public Flux<ExpenseByCategory> getExpensesByCategory(UUID userId, LocalDate from, LocalDate to) {
        return databaseClient.sql("""
                SELECT c.name as category, c.color,
                       SUM(mv.amount) as amount
                FROM movements mv
                JOIN categories c ON c.id = mv.category_id
                WHERE mv.user_id = :userId AND mv.type = 'EXPENSE'
                  AND mv.movement_date >= :from AND mv.movement_date <= :to
                  AND mv.deleted_at IS NULL
                GROUP BY c.name, c.color
                ORDER BY amount DESC
                """)
                .bind("userId", userId)
                .bind("from", from)
                .bind("to", to)
                .map(row -> ExpenseByCategory.builder()
                        .category(row.get("category", String.class))
                        .color(row.get("color", String.class))
                        .amount(row.get("amount", BigDecimal.class))
                        .percentage(0)
                        .build())
                .all()
                .collectList()
                .flatMapMany(list -> {
                    BigDecimal total = list.stream()
                            .map(ExpenseByCategory::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (total.compareTo(BigDecimal.ZERO) == 0) {
                        return Flux.empty();
                    }
                    list.forEach(item -> item.setPercentage(
                            item.getAmount().multiply(BigDecimal.valueOf(100))
                                    .divide(total, 1, java.math.RoundingMode.HALF_UP)
                                    .doubleValue()
                    ));
                    return Flux.fromIterable(list);
                });
    }

    @Override
    public Flux<UpcomingPayment> getUpcomingPayments(UUID userId, int limit) {
        return databaseClient.sql("""
                SELECT sp.id, sp.name, sp.amount, sp.due_date, sp.status,
                       COALESCE(c.name, '') as category
                FROM scheduled_payments sp
                LEFT JOIN categories c ON c.id = sp.category_id
                WHERE sp.user_id = :userId AND sp.status != 'PAID'
                ORDER BY sp.due_date ASC
                LIMIT :limit
                """)
                .bind("userId", userId)
                .bind("limit", limit)
                .map(row -> UpcomingPayment.builder()
                        .id(row.get("id", UUID.class))
                        .name(row.get("name", String.class))
                        .amount(row.get("amount", BigDecimal.class))
                        .dueDate(row.get("due_date", LocalDate.class))
                        .status(row.get("status", String.class))
                        .category(row.get("category", String.class))
                        .build())
                .all();
    }

    @Override
    public Flux<DashboardAlert> getAlerts(UUID userId) {
        return databaseClient.sql("""
                SELECT sp.name, sp.amount, sp.due_date, sp.status
                FROM scheduled_payments sp
                WHERE sp.user_id = :userId
                  AND sp.status = 'OVERDUE'
                ORDER BY sp.due_date ASC
                LIMIT 5
                """)
                .bind("userId", userId)
                .map(row -> DashboardAlert.builder()
                        .type("OVERDUE_PAYMENT")
                        .title(row.get("name", String.class))
                        .description("Vencido - $" + row.get("amount", BigDecimal.class))
                        .severity("HIGH")
                        .build())
                .all();
    }
}
