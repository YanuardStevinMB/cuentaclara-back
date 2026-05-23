package co.com.cuentaclara.r2dbc.loan;

import co.com.cuentaclara.model.loan.*;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LoanAdapter implements LoanGateway {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Loan> save(Loan loan) {
        return databaseClient.sql("""
                INSERT INTO loans (id, contact_id, created_by_user_id, loan_direction, status, title,
                    principal_amount, interest_type, interest_rate, expected_total_amount,
                    loan_date, due_date, description, created_at, updated_at)
                VALUES (:id, :contactId, :userId, :direction, :status, :title,
                    :principal, :interestType, :interestRate, :expectedTotal,
                    :loanDate, :dueDate, :description, :createdAt, :updatedAt)
                """)
                .bind("id", loan.getId())
                .bind("contactId", loan.getContactId())
                .bind("userId", loan.getUserId())
                .bind("direction", loan.getLoanDirection())
                .bind("status", loan.getStatus())
                .bind("title", loan.getTitle())
                .bind("principal", loan.getPrincipalAmount())
                .bind("interestType", loan.getInterestType() != null ? loan.getInterestType() : "NONE")
                .bind("interestRate", loan.getInterestRate() != null ? loan.getInterestRate() : BigDecimal.ZERO)
                .bind("expectedTotal", loan.getExpectedTotalAmount())
                .bind("loanDate", loan.getLoanDate())
                .bind("dueDate", loan.getDueDate())
                .bind("description", loan.getDescription() != null ? loan.getDescription() : "")
                .bind("createdAt", loan.getCreatedAt())
                .bind("updatedAt", loan.getUpdatedAt())
                .then()
                .thenReturn(loan);
    }

    @Override
    public Mono<Loan> findById(UUID id) {
        return databaseClient.sql("""
                SELECT l.*, c.full_name as contact_name,
                    COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.loan_id = l.id AND p.status = 'ACTIVE'), 0) as paid_amount
                FROM loans l
                JOIN contacts c ON c.id = l.contact_id
                WHERE l.id = :id AND l.deleted_at IS NULL
                """)
                .bind("id", id)
                .map(this::mapLoanRow)
                .one();
    }

    @Override
    public Flux<Loan> findByFilter(LoanFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT l.*, c.full_name as contact_name,
                    COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.loan_id = l.id AND p.status = 'ACTIVE'), 0) as paid_amount
                FROM loans l
                JOIN contacts c ON c.id = l.contact_id
                WHERE l.created_by_user_id = :userId AND l.deleted_at IS NULL
                """);
        if (filter.getDirection() != null) sql.append(" AND l.loan_direction = :direction");
        if (filter.getStatus() != null) sql.append(" AND l.status = :status");
        sql.append(" ORDER BY l.loan_date DESC LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("userId", filter.getUserId())
                .bind("limit", filter.getSize())
                .bind("offset", filter.getPage() * filter.getSize());

        if (filter.getDirection() != null) spec = spec.bind("direction", filter.getDirection());
        if (filter.getStatus() != null) spec = spec.bind("status", filter.getStatus());

        return spec.map(this::mapLoanRow).all();
    }

    @Override
    public Mono<Long> countByFilter(LoanFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) as total FROM loans l
                WHERE l.created_by_user_id = :userId AND l.deleted_at IS NULL
                """);
        if (filter.getDirection() != null) sql.append(" AND l.loan_direction = :direction");
        if (filter.getStatus() != null) sql.append(" AND l.status = :status");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("userId", filter.getUserId());

        if (filter.getDirection() != null) spec = spec.bind("direction", filter.getDirection());
        if (filter.getStatus() != null) spec = spec.bind("status", filter.getStatus());

        return spec.map(row -> row.get("total", Long.class)).one();
    }

    @Override
    public Mono<Loan> update(Loan loan) {
        return databaseClient.sql("""
                UPDATE loans SET title = :title, principal_amount = :principal,
                    interest_type = :interestType, interest_rate = :interestRate,
                    expected_total_amount = :expectedTotal, due_date = :dueDate,
                    description = :description, updated_at = :updatedAt
                WHERE id = :id AND deleted_at IS NULL
                """)
                .bind("id", loan.getId())
                .bind("title", loan.getTitle())
                .bind("principal", loan.getPrincipalAmount())
                .bind("interestType", loan.getInterestType() != null ? loan.getInterestType() : "NONE")
                .bind("interestRate", loan.getInterestRate() != null ? loan.getInterestRate() : BigDecimal.ZERO)
                .bind("expectedTotal", loan.getExpectedTotalAmount())
                .bind("dueDate", loan.getDueDate())
                .bind("description", loan.getDescription() != null ? loan.getDescription() : "")
                .bind("updatedAt", loan.getUpdatedAt())
                .then()
                .thenReturn(loan);
    }

    @Override
    public Mono<Void> updateStatus(UUID id, String status) {
        return databaseClient.sql("UPDATE loans SET status = :status, updated_at = NOW() WHERE id = :id")
                .bind("id", id)
                .bind("status", status)
                .then();
    }

    @Override
    public Mono<Void> softDelete(UUID id) {
        return databaseClient.sql("UPDATE loans SET deleted_at = NOW() WHERE id = :id")
                .bind("id", id)
                .then();
    }

    @Override
    public Mono<LoanSummary> getSummary(UUID userId) {
        return databaseClient.sql("""
                SELECT
                    COALESCE(SUM(CASE WHEN loan_direction = 'GIVEN' AND status IN ('ACTIVE','OVERDUE') THEN expected_total_amount ELSE 0 END), 0) as total_given,
                    COALESCE(SUM(CASE WHEN loan_direction = 'RECEIVED' AND status IN ('ACTIVE','OVERDUE') THEN expected_total_amount ELSE 0 END), 0) as total_received,
                    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_count,
                    COUNT(CASE WHEN status = 'OVERDUE' THEN 1 END) as overdue_count
                FROM loans
                WHERE created_by_user_id = :userId AND deleted_at IS NULL
                """)
                .bind("userId", userId)
                .map(row -> {
                    BigDecimal given = row.get("total_given", BigDecimal.class);
                    BigDecimal received = row.get("total_received", BigDecimal.class);
                    return LoanSummary.builder()
                            .totalGiven(given)
                            .totalReceived(received)
                            .balance(received.subtract(given))
                            .activeLoansCount(row.get("active_count", Long.class).intValue())
                            .overdueLoansCount(row.get("overdue_count", Long.class).intValue())
                            .build();
                })
                .one();
    }

    // ===== PAYMENTS =====

    @Override
    public Mono<LoanPayment> savePayment(LoanPayment payment) {
        return databaseClient.sql("""
                INSERT INTO payments (id, loan_id, registered_by_user_id, amount, paid_on,
                    payment_method, description, status, created_at, updated_at)
                VALUES (:id, :loanId, :userId, :amount, :paidOn,
                    :paymentMethod, :description, :status, :createdAt, :updatedAt)
                """)
                .bind("id", payment.getId())
                .bind("loanId", payment.getLoanId())
                .bind("userId", payment.getUserId())
                .bind("amount", payment.getAmount())
                .bind("paidOn", payment.getPaidOn())
                .bind("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "")
                .bind("description", payment.getDescription() != null ? payment.getDescription() : "")
                .bind("status", payment.getStatus())
                .bind("createdAt", payment.getCreatedAt())
                .bind("updatedAt", payment.getUpdatedAt())
                .then()
                .thenReturn(payment);
    }

    @Override
    public Flux<LoanPayment> getPayments(UUID loanId) {
        return databaseClient.sql("""
                SELECT * FROM payments
                WHERE loan_id = :loanId AND deleted_at IS NULL
                ORDER BY paid_on DESC
                """)
                .bind("loanId", loanId)
                .map(row -> LoanPayment.builder()
                        .id(row.get("id", UUID.class))
                        .loanId(row.get("loan_id", UUID.class))
                        .userId(row.get("registered_by_user_id", UUID.class))
                        .amount(row.get("amount", BigDecimal.class))
                        .paidOn(row.get("paid_on", LocalDate.class))
                        .paymentMethod(row.get("payment_method", String.class))
                        .description(row.get("description", String.class))
                        .status(row.get("status", String.class))
                        .createdAt(row.get("created_at", Instant.class))
                        .updatedAt(row.get("updated_at", Instant.class))
                        .build())
                .all();
    }

    @Override
    public Mono<Void> voidPayment(UUID paymentId) {
        return databaseClient.sql("UPDATE payments SET status = 'VOIDED', updated_at = NOW() WHERE id = :id")
                .bind("id", paymentId)
                .then();
    }

    private Loan mapLoanRow(io.r2dbc.spi.Readable row) {
        BigDecimal paid = row.get("paid_amount", BigDecimal.class);
        BigDecimal expected = row.get("expected_total_amount", BigDecimal.class);
        return Loan.builder()
                .id(row.get("id", UUID.class))
                .contactId(row.get("contact_id", UUID.class))
                .userId(row.get("created_by_user_id", UUID.class))
                .loanDirection(row.get("loan_direction", String.class))
                .status(row.get("status", String.class))
                .title(row.get("title", String.class))
                .principalAmount(row.get("principal_amount", BigDecimal.class))
                .interestType(row.get("interest_type", String.class))
                .interestRate(row.get("interest_rate", BigDecimal.class))
                .expectedTotalAmount(expected)
                .loanDate(row.get("loan_date", LocalDate.class))
                .dueDate(row.get("due_date", LocalDate.class))
                .description(row.get("description", String.class))
                .createdAt(row.get("created_at", Instant.class))
                .updatedAt(row.get("updated_at", Instant.class))
                .contactName(row.get("contact_name", String.class))
                .paidAmount(paid)
                .remainingAmount(expected.subtract(paid))
                .build();
    }
}
