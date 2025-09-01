package co.com.crediya.model.creditapplication.gateways;

import co.com.crediya.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {
    Mono<CreditApplication> createCreditApplication(CreditApplication creditApplication);
}
