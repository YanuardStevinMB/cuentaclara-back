package co.com.cuentaclara.api.movement;

import co.com.cuentaclara.model.movement.*;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.usecase.auth.ValidateTokenUseCase;
import co.com.cuentaclara.usecase.movement.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MovementController {

    private final ValidateTokenUseCase validateTokenUseCase;
    private final CreateMovementUseCase createMovementUseCase;
    private final UpdateMovementUseCase updateMovementUseCase;
    private final DeleteMovementUseCase deleteMovementUseCase;
    private final GetMovementsUseCase getMovementsUseCase;
    private final GetMovementSummaryUseCase getMovementSummaryUseCase;
    private final ManageCategoryUseCase manageCategoryUseCase;

    // ===== MOVEMENTS =====

    @PostMapping("/movements")
    public Mono<ResponseEntity<Movement>> create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MovementRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Movement movement = Movement.builder()
                            .userId(user.getId())
                            .categoryId(request.categoryId())
                            .type(MovementType.valueOf(request.type()))
                            .amount(request.amount())
                            .description(request.description())
                            .movementDate(request.movementDate())
                            .scope(FinancialScope.valueOf(request.scope() != null ? request.scope() : "PERSONAL"))
                            .paymentMethod(request.paymentMethod())
                            .build();
                    return createMovementUseCase.execute(movement);
                })
                .map(m -> ResponseEntity.status(HttpStatus.CREATED).body(m));
    }

    @GetMapping("/movements")
    public Mono<ResponseEntity<MovementPage>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    MovementFilter filter = MovementFilter.builder()
                            .userId(user.getId())
                            .type(type != null ? MovementType.valueOf(type) : null)
                            .scope(scope != null ? FinancialScope.valueOf(scope) : null)
                            .categoryId(categoryId)
                            .dateFrom(dateFrom)
                            .dateTo(dateTo)
                            .page(page)
                            .size(size)
                            .build();
                    return getMovementsUseCase.execute(filter);
                })
                .map(ResponseEntity::ok);
    }

    @GetMapping("/movements/summary")
    public Mono<ResponseEntity<MovementSummary>> summary(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    MovementFilter filter = MovementFilter.builder()
                            .userId(user.getId())
                            .type(type != null ? MovementType.valueOf(type) : null)
                            .scope(scope != null ? FinancialScope.valueOf(scope) : null)
                            .categoryId(categoryId)
                            .dateFrom(dateFrom)
                            .dateTo(dateTo)
                            .page(0)
                            .size(0)
                            .build();
                    return getMovementSummaryUseCase.execute(filter);
                })
                .map(ResponseEntity::ok);
    }

    @PutMapping("/movements/{id}")
    public Mono<ResponseEntity<Movement>> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody MovementRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Movement update = Movement.builder()
                            .categoryId(request.categoryId())
                            .amount(request.amount())
                            .description(request.description())
                            .movementDate(request.movementDate())
                            .scope(FinancialScope.valueOf(request.scope() != null ? request.scope() : "PERSONAL"))
                            .paymentMethod(request.paymentMethod())
                            .build();
                    return updateMovementUseCase.execute(id, user.getId(), update);
                })
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/movements/{id}")
    public Mono<ResponseEntity<Void>> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {
        return extractUser(authHeader)
                .flatMap(user -> deleteMovementUseCase.execute(id, user.getId()))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    // ===== CATEGORIES =====

    @GetMapping("/categories")
    public Mono<ResponseEntity<List<Category>>> getCategories(
            @RequestHeader("Authorization") String authHeader) {
        return extractUser(authHeader)
                .flatMapMany(user -> manageCategoryUseCase.getCategories(user.getId()))
                .collectList()
                .map(ResponseEntity::ok);
    }

    @PostMapping("/categories")
    public Mono<ResponseEntity<Category>> createCategory(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CategoryRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Category category = Category.builder()
                            .userId(user.getId())
                            .name(request.name())
                            .type(MovementType.valueOf(request.type()))
                            .icon(request.icon())
                            .color(request.color())
                            .system(false)
                            .build();
                    return manageCategoryUseCase.createCategory(category);
                })
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }

    @PutMapping("/categories/{id}")
    public Mono<ResponseEntity<Category>> updateCategory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody CategoryRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Category update = Category.builder()
                            .name(request.name())
                            .icon(request.icon())
                            .color(request.color())
                            .build();
                    return manageCategoryUseCase.updateCategory(id, user.getId(), update);
                })
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/categories/{id}/status")
    public Mono<ResponseEntity<Void>> toggleCategoryStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody CategoryStatusRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> manageCategoryUseCase.toggleStatus(id, user.getId(), request.active()))
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    private Mono<User> extractUser(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return validateTokenUseCase.execute(token);
    }

    // ===== DTOs =====

    public record MovementRequest(
            UUID categoryId,
            String type,
            BigDecimal amount,
            String description,
            LocalDate movementDate,
            String scope,
            String paymentMethod
    ) {}

    public record CategoryRequest(
            String name,
            String type,
            String icon,
            String color
    ) {}

    public record CategoryStatusRequest(boolean active) {}
}
