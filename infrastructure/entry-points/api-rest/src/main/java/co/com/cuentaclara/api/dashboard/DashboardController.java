package co.com.cuentaclara.api.dashboard;

import co.com.cuentaclara.model.dashboard.DashboardAlert;
import co.com.cuentaclara.model.dashboard.DashboardSummary;
import co.com.cuentaclara.model.dashboard.ExpenseByCategory;
import co.com.cuentaclara.model.dashboard.MonthlyFlow;
import co.com.cuentaclara.model.dashboard.UpcomingPayment;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.usecase.auth.ValidateTokenUseCase;
import co.com.cuentaclara.usecase.dashboard.GetAlertsUseCase;
import co.com.cuentaclara.usecase.dashboard.GetDashboardSummaryUseCase;
import co.com.cuentaclara.usecase.dashboard.GetExpensesByCategoryUseCase;
import co.com.cuentaclara.usecase.dashboard.GetMonthlyFlowUseCase;
import co.com.cuentaclara.usecase.dashboard.GetUpcomingPaymentsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping(value = "/api/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DashboardController {

    private final ValidateTokenUseCase validateTokenUseCase;
    private final GetDashboardSummaryUseCase getDashboardSummaryUseCase;
    private final GetMonthlyFlowUseCase getMonthlyFlowUseCase;
    private final GetUpcomingPaymentsUseCase getUpcomingPaymentsUseCase;
    private final GetAlertsUseCase getAlertsUseCase;
    private final GetExpensesByCategoryUseCase getExpensesByCategoryUseCase;

    @GetMapping("/summary")
    public Mono<ResponseEntity<DashboardSummary>> getSummary(
            @RequestHeader("Authorization") String authHeader) {
        return extractUser(authHeader)
                .flatMap(user -> getDashboardSummaryUseCase.execute(user.getId()))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/monthly-flow")
    public Mono<ResponseEntity<List<MonthlyFlow>>> getMonthlyFlow(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "6") int months) {
        return extractUser(authHeader)
                .flatMapMany(user -> getMonthlyFlowUseCase.execute(user.getId(), months))
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/expenses-by-category")
    public Mono<ResponseEntity<List<ExpenseByCategory>>> getExpensesByCategory(
            @RequestHeader("Authorization") String authHeader) {
        return extractUser(authHeader)
                .flatMapMany(user -> getExpensesByCategoryUseCase.execute(user.getId()))
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/upcoming-payments")
    public Mono<ResponseEntity<List<UpcomingPayment>>> getUpcomingPayments(
            @RequestHeader("Authorization") String authHeader) {
        return extractUser(authHeader)
                .flatMapMany(user -> getUpcomingPaymentsUseCase.execute(user.getId()))
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/alerts")
    public Mono<ResponseEntity<List<DashboardAlert>>> getAlerts(
            @RequestHeader("Authorization") String authHeader) {
        return extractUser(authHeader)
                .flatMapMany(user -> getAlertsUseCase.execute(user.getId()))
                .collectList()
                .map(ResponseEntity::ok);
    }

    private Mono<User> extractUser(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return validateTokenUseCase.execute(token);
    }
}
