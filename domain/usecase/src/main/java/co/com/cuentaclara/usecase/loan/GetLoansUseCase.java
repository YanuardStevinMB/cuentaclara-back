package co.com.cuentaclara.usecase.loan;

import co.com.cuentaclara.model.loan.Loan;
import co.com.cuentaclara.model.loan.LoanFilter;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class GetLoansUseCase {
    private final LoanGateway loanGateway;

    public Mono<List<Loan>> execute(LoanFilter filter) {
        return loanGateway.findByFilter(filter).collectList();
    }

    public Mono<Long> count(LoanFilter filter) {
        return loanGateway.countByFilter(filter);
    }
}
