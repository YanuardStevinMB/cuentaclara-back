package co.com.cuentaclara.r2dbc.contact;

import co.com.cuentaclara.model.contact.Contact;
import co.com.cuentaclara.model.contact.FinancialHistoryItem;
import co.com.cuentaclara.model.contact.gateways.ContactGateway;
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
public class ContactAdapter implements ContactGateway {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Contact> save(Contact contact) {
        return databaseClient.sql("""
                INSERT INTO contacts (id, created_by_user_id, full_name, phone, email, notes, active, created_at, updated_at)
                VALUES (:id, :userId, :fullName, :phone, :email, :notes, :active, :createdAt, :updatedAt)
                """)
                .bind("id", contact.getId())
                .bind("userId", contact.getUserId())
                .bind("fullName", contact.getFullName())
                .bind("phone", contact.getPhone() != null ? contact.getPhone() : "")
                .bind("email", contact.getEmail() != null ? contact.getEmail() : "")
                .bind("notes", contact.getNotes() != null ? contact.getNotes() : "")
                .bind("active", contact.isActive())
                .bind("createdAt", contact.getCreatedAt())
                .bind("updatedAt", contact.getUpdatedAt())
                .then()
                .thenReturn(contact);
    }

    @Override
    public Mono<Contact> findById(UUID id) {
        return databaseClient.sql("""
                SELECT c.*,
                    (SELECT COUNT(*) FROM loans l WHERE l.contact_id = c.id AND l.status = 'ACTIVE') as active_loans_count,
                    (SELECT COUNT(*) FROM loans l WHERE l.contact_id = c.id) as total_loans_count
                FROM contacts c
                WHERE c.id = :id AND c.deleted_at IS NULL
                """)
                .bind("id", id)
                .map(this::mapRow)
                .one();
    }

    @Override
    public Flux<Contact> findByUserId(UUID userId) {
        return databaseClient.sql("""
                SELECT c.*,
                    (SELECT COUNT(*) FROM loans l WHERE l.contact_id = c.id AND l.status = 'ACTIVE') as active_loans_count,
                    (SELECT COUNT(*) FROM loans l WHERE l.contact_id = c.id) as total_loans_count
                FROM contacts c
                WHERE c.created_by_user_id = :userId AND c.deleted_at IS NULL
                ORDER BY c.full_name ASC
                """)
                .bind("userId", userId)
                .map(this::mapRow)
                .all();
    }

    @Override
    public Flux<Contact> findActiveByUserId(UUID userId) {
        return databaseClient.sql("""
                SELECT c.*,
                    (SELECT COUNT(*) FROM loans l WHERE l.contact_id = c.id AND l.status = 'ACTIVE') as active_loans_count,
                    (SELECT COUNT(*) FROM loans l WHERE l.contact_id = c.id) as total_loans_count
                FROM contacts c
                WHERE c.created_by_user_id = :userId AND c.active = true AND c.deleted_at IS NULL
                ORDER BY c.full_name ASC
                """)
                .bind("userId", userId)
                .map(this::mapRow)
                .all();
    }

    @Override
    public Flux<Contact> searchByNameOrPhone(UUID userId, String query) {
        String pattern = "%" + query.toLowerCase() + "%";
        return databaseClient.sql("""
                SELECT c.*,
                    (SELECT COUNT(*) FROM loans l WHERE l.contact_id = c.id AND l.status = 'ACTIVE') as active_loans_count,
                    (SELECT COUNT(*) FROM loans l WHERE l.contact_id = c.id) as total_loans_count
                FROM contacts c
                WHERE c.created_by_user_id = :userId AND c.deleted_at IS NULL
                    AND (LOWER(c.full_name) LIKE :pattern OR c.phone LIKE :pattern)
                ORDER BY c.full_name ASC
                """)
                .bind("userId", userId)
                .bind("pattern", pattern)
                .map(this::mapRow)
                .all();
    }

    @Override
    public Mono<Contact> update(Contact contact) {
        return databaseClient.sql("""
                UPDATE contacts
                SET full_name = :fullName, phone = :phone, email = :email, notes = :notes, updated_at = :updatedAt
                WHERE id = :id AND deleted_at IS NULL
                """)
                .bind("id", contact.getId())
                .bind("fullName", contact.getFullName())
                .bind("phone", contact.getPhone() != null ? contact.getPhone() : "")
                .bind("email", contact.getEmail() != null ? contact.getEmail() : "")
                .bind("notes", contact.getNotes() != null ? contact.getNotes() : "")
                .bind("updatedAt", contact.getUpdatedAt())
                .then()
                .thenReturn(contact);
    }

    @Override
    public Mono<Void> toggleStatus(UUID id, boolean active) {
        return databaseClient.sql("""
                UPDATE contacts SET active = :active, updated_at = NOW() WHERE id = :id
                """)
                .bind("id", id)
                .bind("active", active)
                .then();
    }

    @Override
    public Mono<Boolean> existsByNameAndPhone(UUID userId, String fullName, String phone) {
        return databaseClient.sql("""
                SELECT COUNT(*) as cnt FROM contacts
                WHERE created_by_user_id = :userId AND LOWER(full_name) = LOWER(:fullName)
                    AND phone = :phone AND deleted_at IS NULL
                """)
                .bind("userId", userId)
                .bind("fullName", fullName)
                .bind("phone", phone != null ? phone : "")
                .map(row -> row.get("cnt", Long.class))
                .one()
                .map(count -> count > 0);
    }

    @Override
    public Flux<FinancialHistoryItem> getFinancialHistory(UUID contactId) {
        return databaseClient.sql("""
                SELECT l.id as loan_id, l.title, l.loan_direction, l.status,
                    l.principal_amount, l.loan_date, l.due_date,
                    COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.loan_id = l.id AND p.status = 'ACTIVE'), 0) as paid_amount
                FROM loans l
                WHERE l.contact_id = :contactId AND l.deleted_at IS NULL
                ORDER BY l.loan_date DESC
                """)
                .bind("contactId", contactId)
                .map(row -> FinancialHistoryItem.builder()
                        .loanId(row.get("loan_id", UUID.class))
                        .title(row.get("title", String.class))
                        .direction(row.get("loan_direction", String.class))
                        .status(row.get("status", String.class))
                        .principalAmount(row.get("principal_amount", BigDecimal.class))
                        .paidAmount(row.get("paid_amount", BigDecimal.class))
                        .loanDate(row.get("loan_date", LocalDate.class))
                        .dueDate(row.get("due_date", LocalDate.class))
                        .build())
                .all();
    }

    private Contact mapRow(io.r2dbc.spi.Readable row) {
        return Contact.builder()
                .id(row.get("id", UUID.class))
                .userId(row.get("created_by_user_id", UUID.class))
                .fullName(row.get("full_name", String.class))
                .phone(row.get("phone", String.class))
                .email(row.get("email", String.class))
                .notes(row.get("notes", String.class))
                .active(Boolean.TRUE.equals(row.get("active", Boolean.class)))
                .createdAt(row.get("created_at", Instant.class))
                .updatedAt(row.get("updated_at", Instant.class))
                .activeLoansCount(row.get("active_loans_count", Long.class) != null ? row.get("active_loans_count", Long.class).intValue() : 0)
                .totalLoansCount(row.get("total_loans_count", Long.class) != null ? row.get("total_loans_count", Long.class).intValue() : 0)
                .build();
    }
}
