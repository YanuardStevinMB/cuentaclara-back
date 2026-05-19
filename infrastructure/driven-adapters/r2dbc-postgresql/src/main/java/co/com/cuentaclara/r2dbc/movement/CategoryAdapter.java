package co.com.cuentaclara.r2dbc.movement;

import co.com.cuentaclara.model.movement.Category;
import co.com.cuentaclara.model.movement.MovementType;
import co.com.cuentaclara.model.movement.gateways.CategoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryAdapter implements CategoryGateway {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Category> save(Category category) {
        return databaseClient.sql("""
                INSERT INTO categories (id, user_id, name, type, icon, color, is_system, active, created_at)
                VALUES (:id, :userId, :name, :type, :icon, :color, :isSystem, :active, :createdAt)
                """)
                .bind("id", category.getId())
                .bind("userId", category.getUserId())
                .bind("name", category.getName())
                .bind("type", category.getType().name())
                .bind("icon", category.getIcon() != null ? category.getIcon() : "")
                .bind("color", category.getColor() != null ? category.getColor() : "#6b7280")
                .bind("isSystem", category.isSystem())
                .bind("active", category.isActive())
                .bind("createdAt", category.getCreatedAt())
                .then()
                .thenReturn(category);
    }

    @Override
    public Mono<Category> findById(UUID id) {
        return databaseClient.sql("SELECT * FROM categories WHERE id = :id")
                .bind("id", id)
                .map(this::mapRow)
                .one();
    }

    @Override
    public Flux<Category> findByUserId(UUID userId) {
        return databaseClient.sql("""
                SELECT * FROM categories
                WHERE (user_id = :userId OR user_id IS NULL)
                ORDER BY is_system DESC, name ASC
                """)
                .bind("userId", userId)
                .map(this::mapRow)
                .all();
    }

    @Override
    public Flux<Category> findActiveByUserIdAndType(UUID userId, MovementType type) {
        return databaseClient.sql("""
                SELECT * FROM categories
                WHERE (user_id = :userId OR user_id IS NULL) AND active = TRUE AND type = :type
                ORDER BY is_system DESC, name ASC
                """)
                .bind("userId", userId)
                .bind("type", type.name())
                .map(this::mapRow)
                .all();
    }

    @Override
    public Mono<Category> update(Category category) {
        return databaseClient.sql("""
                UPDATE categories SET name = :name, icon = :icon, color = :color
                WHERE id = :id
                """)
                .bind("id", category.getId())
                .bind("name", category.getName())
                .bind("icon", category.getIcon() != null ? category.getIcon() : "")
                .bind("color", category.getColor() != null ? category.getColor() : "#6b7280")
                .then()
                .thenReturn(category);
    }

    @Override
    public Mono<Void> toggleStatus(UUID id, UUID userId, boolean active) {
        return databaseClient.sql("""
                UPDATE categories SET active = :active
                WHERE id = :id AND (user_id = :userId OR (is_system = FALSE AND user_id = :userId))
                """)
                .bind("id", id)
                .bind("userId", userId)
                .bind("active", active)
                .then();
    }

    private Category mapRow(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata meta) {
        return Category.builder()
                .id(row.get("id", UUID.class))
                .userId(row.get("user_id", UUID.class))
                .name(row.get("name", String.class))
                .type(MovementType.valueOf(row.get("type", String.class)))
                .icon(row.get("icon", String.class))
                .color(row.get("color", String.class))
                .system(Boolean.TRUE.equals(row.get("is_system", Boolean.class)))
                .active(Boolean.TRUE.equals(row.get("active", Boolean.class)))
                .createdAt(row.get("created_at", Instant.class))
                .build();
    }
}
