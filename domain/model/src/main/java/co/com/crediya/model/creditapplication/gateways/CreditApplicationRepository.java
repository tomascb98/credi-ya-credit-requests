package co.com.crediya.model.creditapplication.gateways;

import co.com.crediya.model.creditapplication.CreditApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {
    Mono<CreditApplication> createCreditApplication(CreditApplication creditApplication);
    Flux<CreditApplication> getCreditApplications(int skip, int pageSize, String loanStatus);
    Mono<Long> countCreditApplications(String filter);
}
