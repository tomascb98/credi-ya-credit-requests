package co.com.crediya.usecase.creditapplication;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.model.user.gateways.UserService;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.usecase.creditapplication.helper.CreditApplicationValidatorHelper;
import reactor.core.publisher.Mono;

public class CreditApplicationUseCase {
    private final CreditApplicationRepository creditApplicationRepository;
    private final UserService userService;
    private final LoanTypeRepository loanTypeRepository;

    public CreditApplicationUseCase(CreditApplicationRepository creditApplicationRepository, 
                                   UserService userService,
                                   LoanTypeRepository loanTypeRepository) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.userService = userService;
        this.loanTypeRepository = loanTypeRepository;
    }

    public Mono<CreditApplication> createCreditApplication(CreditApplication creditApplication, String jwtToken) {
        return CreditApplicationValidatorHelper.validateAndSaveCreditApplication(creditApplication, jwtToken, creditApplicationRepository, userService, loanTypeRepository);
    }
}
