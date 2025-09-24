package co.com.crediya.model.loantype.gateways;

import co.com.crediya.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<LoanType> findById(String id);
    Mono<Boolean> existsByName(String name);
    Mono<LoanType> findByName(String name);
    Mono<Boolean> hasAutomaticValidation(Integer loanTypeId);
}
