package co.com.crediya.usecase.creditapplication.helper;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.model.exceptions.UserNotFoundException;
import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requeststate.RequestState;
import co.com.crediya.model.user.gateways.UserService;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditApplicationValidatorHelperTest {

    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private LoanTypeRepository loanTypeRepository;
    
    private CreditApplication validCreditApplication;
    private LoanType validLoanType;
    private RequestState pendingState;

    @BeforeEach
    void setUp() {
        // Setup valid entities
        validLoanType = LoanType.builder()
            .id(1)
            .name("PERSONAL")
            .minimumAmount(BigDecimal.valueOf(1000000))
            .maximumAmount(BigDecimal.valueOf(50000000))
            .build();
            
        pendingState = RequestState.builder()
            .id(1)
            .name("PENDIENTE")
            .description("Solicitud en revisión")
            .build();
            
        validCreditApplication = CreditApplication.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(validLoanType)
            .requestState(pendingState)
            .build();
    }

    @Test
    @DisplayName("Debería validar y guardar solicitud de crédito exitosamente")
    void shouldValidateAndSaveCreditApplicationSuccessfully() {
        // Arrange
        when(userService.validateUser(validCreditApplication.getDocumentNumber()))
            .thenReturn(Mono.just(true));
        when(loanTypeRepository.findByName(validCreditApplication.getLoanType().getName()))
            .thenReturn(Mono.just(validLoanType));
        when(creditApplicationRepository.createCreditApplication(any(CreditApplication.class)))
            .thenReturn(Mono.just(validCreditApplication));

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(validCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectNext(validCreditApplication)
            .verifyComplete();
            
        verify(userService).validateUser(validCreditApplication.getDocumentNumber());
        verify(loanTypeRepository).findByName(validCreditApplication.getLoanType().getName());
        verify(creditApplicationRepository).createCreditApplication(any(CreditApplication.class));
    }

    @Test
    @DisplayName("Debería fallar validación cuando el email está vacío")
    void shouldFailValidationWhenEmailIsEmpty() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("") // empty email
            .documentNumber("12345678")
            .loanType(validLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(invalidCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar validación cuando el email es null")
    void shouldFailValidationWhenEmailIsNull() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email(null) // null email
            .documentNumber("12345678")
            .loanType(validLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(invalidCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar validación cuando el monto es null")
    void shouldFailValidationWhenAmountIsNull() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(null) // null amount
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(validLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(invalidCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar validación cuando el plazo es null")
    void shouldFailValidationWhenMonthTermIsNull() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(null) // null monthTerm
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(validLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(invalidCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar validación cuando el número de documento es null")
    void shouldFailValidationWhenDocumentNumberIsNull() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber(null) // null documentNumber
            .loanType(validLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(invalidCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar validación cuando el tipo de préstamo es null")
    void shouldFailValidationWhenLoanTypeIsNull() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(null) // null loanType
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(invalidCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar validación cuando el nombre del tipo de préstamo está vacío")
    void shouldFailValidationWhenLoanTypeNameIsEmpty() {
        // Arrange
        LoanType emptyNameLoanType = LoanType.builder()
            .id(1)
            .name("") // empty name
            .minimumAmount(BigDecimal.valueOf(1000000))
            .maximumAmount(BigDecimal.valueOf(50000000))
            .build();
            
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(emptyNameLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(invalidCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar validación cuando el nombre del tipo de préstamo es null")
    void shouldFailValidationWhenLoanTypeNameIsNull() {
        // Arrange
        LoanType nullNameLoanType = LoanType.builder()
            .id(1)
            .name(null) // null name
            .minimumAmount(BigDecimal.valueOf(1000000))
            .maximumAmount(BigDecimal.valueOf(50000000))
            .build();
            
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(nullNameLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(invalidCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando el usuario no existe")
    void shouldFailWhenUserDoesNotExist() {
        // Arrange
        when(userService.validateUser(validCreditApplication.getDocumentNumber()))
            .thenReturn(Mono.just(false));

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(validCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(UserNotFoundException.class)
            .verify();
            
        verify(userService).validateUser(validCreditApplication.getDocumentNumber());
        verifyNoInteractions(loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando el tipo de préstamo no existe")
    void shouldFailWhenLoanTypeDoesNotExist() {
        // Arrange
        when(userService.validateUser(validCreditApplication.getDocumentNumber()))
            .thenReturn(Mono.just(true));
        when(loanTypeRepository.findByName(validCreditApplication.getLoanType().getName()))
            .thenReturn(Mono.empty());

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(validCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verify(userService).validateUser(validCreditApplication.getDocumentNumber());
        verify(loanTypeRepository).findByName(validCreditApplication.getLoanType().getName());
        verifyNoInteractions(creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería establecer estado inicial correctamente")
    void shouldSetInitialRequestStateCorrectly() {
        // Arrange
        CreditApplication creditApplicationWithoutState = CreditApplication.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(validLoanType)
            .build();
            
        when(userService.validateUser(creditApplicationWithoutState.getDocumentNumber()))
            .thenReturn(Mono.just(true));
        when(loanTypeRepository.findByName(creditApplicationWithoutState.getLoanType().getName()))
            .thenReturn(Mono.just(validLoanType));
        when(creditApplicationRepository.createCreditApplication(any(CreditApplication.class)))
            .thenReturn(Mono.just(creditApplicationWithoutState));

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(creditApplicationWithoutState, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectNext(creditApplicationWithoutState)
            .verifyComplete();
            
        // Verify that the request state was set
        verify(creditApplicationRepository).createCreditApplication(argThat(app -> 
            app.getRequestState() != null && 
            app.getRequestState().getId() == 1 && 
            "PENDIENTE".equals(app.getRequestState().getName())
        ));
    }

    @Test
    @DisplayName("Debería completar el tipo de préstamo correctamente")
    void shouldCompleteLoanTypeCorrectly() {
        // Arrange
        CreditApplication creditApplicationWithBasicLoanType = CreditApplication.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(LoanType.builder().name("PERSONAL").build()) // Basic loan type
            .build();
            
        when(userService.validateUser(creditApplicationWithBasicLoanType.getDocumentNumber()))
            .thenReturn(Mono.just(true));
        when(loanTypeRepository.findByName("PERSONAL"))
            .thenReturn(Mono.just(validLoanType));
        when(creditApplicationRepository.createCreditApplication(any(CreditApplication.class)))
            .thenReturn(Mono.just(creditApplicationWithBasicLoanType));

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(creditApplicationWithBasicLoanType, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectNext(creditApplicationWithBasicLoanType)
            .verifyComplete();
            
        // Verify that the loan type was completed
        verify(creditApplicationRepository).createCreditApplication(argThat(app -> 
            app.getLoanType() != null && 
            app.getLoanType().getId() == 1 && 
            "PERSONAL".equals(app.getLoanType().getName())
        ));
    }

    @Test
    @DisplayName("Debería manejar email con espacios en blanco")
    void shouldHandleEmailWithWhitespace() {
        // Arrange
        CreditApplication creditApplicationWithWhitespaceEmail = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("   ") // whitespace email
            .documentNumber("12345678")
            .loanType(validLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(creditApplicationWithWhitespaceEmail, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería manejar nombre de tipo de préstamo con espacios en blanco")
    void shouldHandleLoanTypeNameWithWhitespace() {
        // Arrange
        LoanType whitespaceNameLoanType = LoanType.builder()
            .id(1)
            .name("   ") // whitespace name
            .minimumAmount(BigDecimal.valueOf(1000000))
            .maximumAmount(BigDecimal.valueOf(50000000))
            .build();
            
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(whitespaceNameLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = CreditApplicationValidatorHelper
            .validateAndSaveCreditApplication(invalidCreditApplication, creditApplicationRepository, userService, loanTypeRepository);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }
}
