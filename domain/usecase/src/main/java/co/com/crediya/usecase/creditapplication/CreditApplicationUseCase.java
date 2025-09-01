package co.com.crediya.usecase.creditapplication;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.usecase.creditapplication.helper.CreditApplicationValidatorHelper;
import reactor.core.publisher.Mono;

public class CreditApplicationUseCase {
    private final CreditApplicationRepository creditApplicationRepository;

    public CreditApplicationUseCase(CreditApplicationRepository creditApplicationRepository) {
        this.creditApplicationRepository = creditApplicationRepository;
    }

    public Mono<CreditApplication> createCreditApplication(CreditApplication creditApplication) {
        return CreditApplicationValidatorHelper.validateAndSaveCreditApplication(creditApplication, creditApplicationRepository);
    }
}
