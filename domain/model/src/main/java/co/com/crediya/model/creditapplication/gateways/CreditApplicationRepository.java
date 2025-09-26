package co.com.crediya.model.creditapplication.gateways;

import co.com.crediya.model.creditapplication.CreditApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CreditApplicationRepository {
    Mono<CreditApplication> createCreditApplication(CreditApplication creditApplication);
    Flux<CreditApplication> getCreditApplications(int skip, int pageSize, String loanStatus);
    Mono<Long> countCreditApplications(String filter);
    Mono<CreditApplication> updateApplicationStatus(UUID applicationId, Integer statusId);
    Flux<CreditApplication> getActiveLoansByDocumentNumber(String documentNumber);
}
