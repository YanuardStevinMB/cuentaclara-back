package co.com.cuentaclara.usecase.movement;

import co.com.cuentaclara.model.movement.Category;
import co.com.cuentaclara.model.movement.gateways.CategoryGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class ManageCategoryUseCase {
    private final CategoryGateway categoryGateway;

    public Flux<Category> getCategories(UUID userId) {
        return categoryGateway.findByUserId(userId);
    }

    public Mono<Category> createCategory(Category category) {
        category.setId(UUID.randomUUID());
        category.setCreatedAt(Instant.now());
        category.setActive(true);
        return categoryGateway.save(category);
    }

    public Mono<Category> updateCategory(UUID id, UUID userId, Category update) {
        return categoryGateway.findById(id)
                .flatMap(existing -> {
                    if (existing.isSystem()) {
                        return Mono.error(new RuntimeException("Cannot modify system categories"));
                    }
                    if (existing.getUserId() != null && !existing.getUserId().equals(userId)) {
                        return Mono.error(new RuntimeException("Unauthorized"));
                    }
                    existing.setName(update.getName());
                    existing.setIcon(update.getIcon());
                    existing.setColor(update.getColor());
                    return categoryGateway.update(existing);
                });
    }

    public Mono<Void> toggleStatus(UUID id, UUID userId, boolean active) {
        return categoryGateway.toggleStatus(id, userId, active);
    }
}
