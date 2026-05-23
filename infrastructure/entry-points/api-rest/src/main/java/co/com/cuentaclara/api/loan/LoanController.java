package co.com.cuentaclara.api.loan;

import co.com.cuentaclara.model.loan.*;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.usecase.auth.ValidateTokenUseCase;
import co.com.cuentaclara.usecase.loan.*;
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
@RequestMapping(value = "/api/loans", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LoanController {

    private final ValidateTokenUseCase validateTokenUseCase;
    private final CreateLoanUseCase createLoanUseCase;
    private final GetLoansUseCase getLoansUseCase;
    private final UpdateLoanUseCase updateLoanUseCase;
    private final ManagePaymentsUseCase managePaymentsUseCase;
    private final GetLoanSummaryUseCase getLoanSummaryUseCase;

    @PostMapping
    public Mono<ResponseEntity<Loan>> create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LoanRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Loan loan = Loan.builder()
                            .userId(user.getId())
                            .contactId(request.contactId())
                            .loanDirection(request.direction())
                            .title(request.title())
                            .principalAmount(request.amount())
                            .interestType(request.interestType() != null ? request.interestType() : "NONE")
                            .interestRate(request.interestRate())
                            .expectedTotalAmount(request.expectedTotal() != null ? request.expectedTotal() : request.amount())
                            .loanDate(request.loanDate() != null ? request.loanDate() : LocalDate.now())
                            .dueDate(request.dueDate())
                            .description(request.description())
                            .build();
                    return createLoanUseCase.execute(loan);
                })
                .map(l -> ResponseEntity.status(HttpStatus.CREATED).body(l));
    }

    @GetMapping
    public Mono<ResponseEntity<List<Loan>>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String status) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    LoanFilter filter = LoanFilter.builder()
                            .userId(user.getId())
                            .direction(direction)
                            .status(status)
                            .page(page)
                            .size(size)
                            .build();
                    return getLoansUseCase.execute(filter);
                })
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Loan>> getById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {
        return extractUser(authHeader)
                .flatMap(user -> getLoansUseCase.execute(
                        LoanFilter.builder().userId(user.getId()).page(0).size(1).build())
                        .then(Mono.defer(() -> {
                            // Direct lookup with ownership check in adapter
                            return Mono.from(
                                    getLoansUseCase.execute(LoanFilter.builder().userId(user.getId()).page(0).size(999).build())
                            ).map(loans -> loans.stream().filter(l -> l.getId().equals(id)).findFirst().orElse(null));
                        })))
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Loan>> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody LoanUpdateRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Loan loan = Loan.builder()
                            .id(id)
                            .title(request.title())
                            .principalAmount(request.amount())
                            .interestType(request.interestType())
                            .interestRate(request.interestRate())
                            .expectedTotalAmount(request.expectedTotal() != null ? request.expectedTotal() : request.amount())
                            .dueDate(request.dueDate())
                            .description(request.description())
                            .build();
                    return updateLoanUseCase.execute(loan);
                })
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<Void>> updateStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody StatusRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Loan loan = Loan.builder().id(id).userId(user.getId()).build();
                    return updateLoanUseCase.updateStatus(loan, request.status());
                })
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @GetMapping("/summary")
    public Mono<ResponseEntity<LoanSummary>> summary(
            @RequestHeader("Authorization") String authHeader) {
        return extractUser(authHeader)
                .flatMap(user -> getLoanSummaryUseCase.execute(user.getId()))
                .map(ResponseEntity::ok);
    }

    // ===== PAYMENTS =====

    @PostMapping("/{loanId}/payments")
    public Mono<ResponseEntity<LoanPayment>> registerPayment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID loanId,
            @RequestBody PaymentRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    LoanPayment payment = LoanPayment.builder()
                            .loanId(loanId)
                            .userId(user.getId())
                            .amount(request.amount())
                            .paidOn(request.paidOn() != null ? request.paidOn() : LocalDate.now())
                            .paymentMethod(request.paymentMethod())
                            .description(request.description())
                            .build();
                    return managePaymentsUseCase.registerPayment(payment);
                })
                .map(p -> ResponseEntity.status(HttpStatus.CREATED).body(p));
    }

    @GetMapping("/{loanId}/payments")
    public Mono<ResponseEntity<List<LoanPayment>>> getPayments(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID loanId) {
        return extractUser(authHeader)
                .flatMap(user -> managePaymentsUseCase.getPayments(loanId).collectList())
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/payments/{paymentId}/void")
    public Mono<ResponseEntity<Void>> voidPayment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID paymentId) {
        return extractUser(authHeader)
                .flatMap(user -> managePaymentsUseCase.voidPayment(paymentId))
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    // ===== HELPERS =====

    private Mono<User> extractUser(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return validateTokenUseCase.execute(token);
    }

    // ===== DTOs =====

    public record LoanRequest(
            UUID contactId,
            String direction,
            String title,
            BigDecimal amount,
            String interestType,
            BigDecimal interestRate,
            BigDecimal expectedTotal,
            LocalDate loanDate,
            LocalDate dueDate,
            String description
    ) {}

    public record LoanUpdateRequest(
            String title,
            BigDecimal amount,
            String interestType,
            BigDecimal interestRate,
            BigDecimal expectedTotal,
            LocalDate dueDate,
            String description
    ) {}

    public record StatusRequest(String status) {}

    public record PaymentRequest(
            BigDecimal amount,
            LocalDate paidOn,
            String paymentMethod,
            String description
    ) {}
}
