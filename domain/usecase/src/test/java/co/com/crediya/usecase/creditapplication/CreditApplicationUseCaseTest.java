package co.com.crediya.usecase.creditapplication;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditApplicationUseCaseTest {

    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private LoanTypeRepository loanTypeRepository;
    
    private CreditApplicationUseCase creditApplicationUseCase;
    
    private CreditApplication validCreditApplication;
    private LoanType validLoanType;
    private RequestState pendingState;

    @BeforeEach
    void setUp() {
        creditApplicationUseCase = new CreditApplicationUseCase(
            creditApplicationRepository, 
            userService, 
            loanTypeRepository
        );
        
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
    @DisplayName("Debería crear solicitud de crédito exitosamente cuando todos los datos son válidos")
    void shouldCreateCreditApplicationSuccessfully() {
        // Arrange
        when(userService.validateUser(validCreditApplication.getDocumentNumber()))
            .thenReturn(Mono.just(true));
        when(loanTypeRepository.findByName(validCreditApplication.getLoanType().getName()))
            .thenReturn(Mono.just(validLoanType));
        when(creditApplicationRepository.createCreditApplication(any(CreditApplication.class)))
            .thenReturn(Mono.just(validCreditApplication));

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(validCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectNext(validCreditApplication)
            .verifyComplete();
            
        verify(userService).validateUser(validCreditApplication.getDocumentNumber());
        verify(loanTypeRepository).findByName(validCreditApplication.getLoanType().getName());
        verify(creditApplicationRepository).createCreditApplication(any(CreditApplication.class));
    }

    @Test
    @DisplayName("Debería fallar cuando el usuario no existe")
    void shouldFailWhenUserDoesNotExist() {
        // Arrange
        when(userService.validateUser(validCreditApplication.getDocumentNumber()))
            .thenReturn(Mono.just(false));

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(validCreditApplication);

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
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(validCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verify(userService).validateUser(validCreditApplication.getDocumentNumber());
        verify(loanTypeRepository).findByName(validCreditApplication.getLoanType().getName());
        verifyNoInteractions(creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando hay campos requeridos faltantes")
    void shouldFailWhenRequiredFieldsAreMissing() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            // email missing
            .documentNumber("12345678")
            .loanType(validLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(invalidCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando el email está vacío")
    void shouldFailWhenEmailIsEmpty() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("") // empty email
            .documentNumber("12345678")
            .loanType(validLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(invalidCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando el monto es null")
    void shouldFailWhenAmountIsNull() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(null) // null amount
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(validLoanType)
            .build();

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(invalidCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando el plazo es null")
    void shouldFailWhenMonthTermIsNull() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(null) // null monthTerm
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(validCreditApplication.getLoanType())
            .build();

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(invalidCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando el número de documento es null")
    void shouldFailWhenDocumentNumberIsNull() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber(null) // null documentNumber
            .loanType(validCreditApplication.getLoanType())
            .build();

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(invalidCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando el tipo de préstamo es null")
    void shouldFailWhenLoanTypeIsNull() {
        // Arrange
        CreditApplication invalidCreditApplication = CreditApplication.builder()
            .amount(BigDecimal.valueOf(5000000))
            .monthTerm(24)
            .email("test@example.com")
            .documentNumber("12345678")
            .loanType(null) // null loanType
            .build();

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(invalidCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando el nombre del tipo de préstamo está vacío")
    void shouldFailWhenLoanTypeNameIsEmpty() {
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
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(invalidCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería fallar cuando el nombre del tipo de préstamo es null")
    void shouldFailWhenLoanTypeNameIsNull() {
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
        Mono<CreditApplication> result = creditApplicationUseCase.createCreditApplication(invalidCreditApplication);

        // Assert
        StepVerifier.create(result)
            .expectError(ValidationException.class)
            .verify();
            
        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }
}
