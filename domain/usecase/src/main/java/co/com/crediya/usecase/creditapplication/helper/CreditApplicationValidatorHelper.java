package co.com.crediya.usecase.creditapplication.helper;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.model.exceptions.UserNotFoundException;
import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.model.user.gateways.UserService;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.requeststate.RequestState;
import reactor.core.publisher.Mono;

public class CreditApplicationValidatorHelper {
    public static Mono<CreditApplication> validateAndSaveCreditApplication(CreditApplication creditApplication,
                                                                           String jwtToken,
                                                                           CreditApplicationRepository creditApplicationRepository,
                                                                           UserService userService,
                                                                           LoanTypeRepository loanTypeRepository) {
        return validateRequiredFields(creditApplication)
                .flatMap(validatedApplication -> validateExistanceUser(validatedApplication.getDocumentNumber(), jwtToken, userService, validatedApplication))
                .flatMap(validatedApplication -> validateAndCompleteLoanType(validatedApplication.getLoanType().getName(), loanTypeRepository, validatedApplication))
                .flatMap(validatedApplication -> setInitialRequestState(validatedApplication))
                .flatMap(creditApplicationRepository::createCreditApplication);
    }

    private static Mono<CreditApplication> validateRequiredFields(CreditApplication creditApplication) {
        return Mono.defer(() -> {
            boolean invalid =
                    isBlank(creditApplication.getEmail()) ||
                            creditApplication.getAmount() == null ||
                            creditApplication.getMonthTerm() == null ||
                            creditApplication.getDocumentNumber() == null ||
                            creditApplication.getLoanType() == null ||
                            isBlank(creditApplication.getLoanType().getName());

            return invalid
                    ? Mono.error(new ValidationException("Los campos email, monto, plazo, numero de documento y tipo de prestamo son obligatorios."))
                    : Mono.just(creditApplication);
        });
    }

    private static boolean isBlank (String s) {
        return s == null || s.trim().isEmpty();
    }

    private static Mono<CreditApplication> validateExistanceUser(String documentNumber, String jwtToken, UserService userService, CreditApplication creditApplication) {
        return userService.validateUser(documentNumber, jwtToken)
                .flatMap(isValid -> isValid 
                    ? Mono.just(creditApplication)
                    : Mono.error(new UserNotFoundException("El usuario no se encuentra registrado en el sistema."))
                );
    }
    
    private static Mono<CreditApplication> validateAndCompleteLoanType(String loanTypeName, LoanTypeRepository loanTypeRepository, CreditApplication creditApplication) {
        return loanTypeRepository.findByName(loanTypeName)
                .flatMap(loanType -> {
                    creditApplication.setLoanType(loanType);
                    return Mono.just(creditApplication);
                })
                .switchIfEmpty(Mono.error(new ValidationException("El tipo de préstamo '" + loanTypeName + "' no es válido o no está disponible.")));
    }
    
    private static Mono<CreditApplication> setInitialRequestState(CreditApplication creditApplication) {
        // Establecer estado inicial como "PENDIENTE" (ID = 1 según el SQL)
        RequestState pendingState = RequestState.builder()
                .id(1)
                .name("PENDIENTE")
                .description("Solicitud en revisión")
                .build();
        
        creditApplication.setRequestState(pendingState);
        return Mono.just(creditApplication);
    }
}
