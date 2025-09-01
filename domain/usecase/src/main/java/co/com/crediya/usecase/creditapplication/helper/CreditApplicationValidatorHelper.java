package co.com.crediya.usecase.creditapplication.helper;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.model.exceptions.ValidationException;
import reactor.core.publisher.Mono;

public class CreditApplicationValidatorHelper {
    public static Mono<CreditApplication> validateAndSaveCreditApplication(CreditApplication creditApplication, CreditApplicationRepository creditApplicationRepository) {
        return validateRequiredFields(creditApplication)
                .flatMap(creditApplicationRepository::createCreditApplication);
    }

    public static Mono<CreditApplication> validateRequiredFields(CreditApplication creditApplication) {
        return Mono.defer(() -> {
            boolean invalid =
                    isBlank(creditApplication.getEmail()) ||
                            creditApplication.getAmount() == null ||
                            creditApplication.getMonthTerm() == null ||
                            creditApplication.getDocumentNumber() == null ||
                            creditApplication.getLoanType().getId() == null;

            return invalid
                    ? Mono.error(new ValidationException("Los campos email, monto, plazo, numero de documento y tipo de prestamo son obligatorios."))
                    : Mono.just(creditApplication);
        });
    }

    private static boolean isBlank (String s) {
        return s == null || s.trim().isEmpty();
    }
}
