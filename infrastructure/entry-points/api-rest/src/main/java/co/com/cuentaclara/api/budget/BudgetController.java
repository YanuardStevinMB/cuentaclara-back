package co.com.cuentaclara.api.budget;

import co.com.cuentaclara.model.budget.Budget;
import co.com.cuentaclara.model.budget.BudgetSummary;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.usecase.auth.ValidateTokenUseCase;
import co.com.cuentaclara.usecase.budget.CreateBudgetUseCase;
import co.com.cuentaclara.usecase.budget.GetBudgetsUseCase;
import co.com.cuentaclara.usecase.budget.UpdateBudgetUseCase;
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
@RequestMapping(value = "/api/budgets", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BudgetController {

    private final ValidateTokenUseCase validateTokenUseCase;
    private final CreateBudgetUseCase createBudgetUseCase;
    private final GetBudgetsUseCase getBudgetsUseCase;
    private final UpdateBudgetUseCase updateBudgetUseCase;

    @PostMapping
    public Mono<ResponseEntity<Budget>> create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BudgetRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Budget budget = Budget.builder()
                            .userId(user.getId())
                            .categoryId(request.categoryId())
                            .periodMonth(request.month())
                            .periodYear(request.year())
                            .limitAmount(request.limitAmount())
                            .alertThresholdPercent(request.alertThreshold() != null ? request.alertThreshold() : 80)
                            .build();
                    return createBudgetUseCase.execute(budget);
                })
                .map(b -> ResponseEntity.status(HttpStatus.CREATED).body(b))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()));
    }

    @GetMapping
    public Mono<ResponseEntity<List<Budget>>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    int m = month != null ? month : LocalDate.now().getMonthValue();
                    int y = year != null ? year : LocalDate.now().getYear();
                    return getBudgetsUseCase.execute(user.getId(), m, y);
                })
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Budget>> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody BudgetUpdateRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Budget budget = Budget.builder()
                            .id(id)
                            .limitAmount(request.limitAmount())
                            .alertThresholdPercent(request.alertThreshold() != null ? request.alertThreshold() : 80)
                            .build();
                    return updateBudgetUseCase.execute(budget);
                })
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<Void>> toggleStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody StatusRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> updateBudgetUseCase.toggleStatus(id, request.active()))
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @GetMapping("/summary")
    public Mono<ResponseEntity<BudgetSummary>> summary(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    int m = month != null ? month : LocalDate.now().getMonthValue();
                    int y = year != null ? year : LocalDate.now().getYear();
                    return getBudgetsUseCase.getSummary(user.getId(), m, y);
                })
                .map(ResponseEntity::ok);
    }

    private Mono<User> extractUser(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return validateTokenUseCase.execute(token);
    }

    public record BudgetRequest(
            UUID categoryId,
            int month,
            int year,
            BigDecimal limitAmount,
            Integer alertThreshold
    ) {}

    public record BudgetUpdateRequest(
            BigDecimal limitAmount,
            Integer alertThreshold
    ) {}

    public record StatusRequest(boolean active) {}
}
