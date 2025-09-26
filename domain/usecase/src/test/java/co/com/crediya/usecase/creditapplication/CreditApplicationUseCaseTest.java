package co.com.crediya.usecase.creditapplication;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.model.exceptions.UserNotFoundException;
import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.notification.gateways.NotificationGateway;
import co.com.crediya.model.requeststate.RequestState;
import co.com.crediya.model.user.gateways.UserModel;
import co.com.crediya.model.user.gateways.UserService;
import co.com.crediya.model.loancapacity.gateways.LoanCapacityGateway;
import co.com.crediya.usecase.creditapplication.helper.CreditApplicationValidatorHelper;
import co.com.crediya.usecase.creditapplication.helper.MonthlyPaymentCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditApplicationUseCaseTest {

    @Mock
    private CreditApplicationRepository creditApplicationRepository;

    @Mock
    private UserService userService;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private NotificationGateway notificationGateway;

    @Mock
    private LoanCapacityGateway loanCapacityGateway;

    private CreditApplicationUseCase useCase;

    private CreditApplication baseApplication;

    @BeforeEach
    void setUp() {
        useCase = new CreditApplicationUseCase(
                creditApplicationRepository,
                userService,
                loanTypeRepository,
                notificationGateway,
                loanCapacityGateway
        );

        baseApplication = CreditApplication.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1_000_000))
                .monthTerm(12)
                .email("user@test.com")
                .documentNumber("123")
                .loanType(LoanType.builder().id(1).name("PERSONAL").interestRate(12.0).build())
                .requestState(RequestState.builder().id(1).name("PENDIENTE").build())
                .build();
    }

    @Test
    @DisplayName("updateApplicationStatus (con razón) delega al repositorio y dispara notificación")
    void updateStatusWithReason_sendsNotification() {
        UUID id = baseApplication.getId();
        when(creditApplicationRepository.updateApplicationStatus(id, 2))
                .thenReturn(Mono.just(baseApplication));
        when(notificationGateway.sendNotification(anyString()))
                .thenReturn(Mono.just("msg-1"));

        Mono<CreditApplication> result = useCase.updateApplicationStatus(id, 2, "OK");

        StepVerifier.create(result)
                .expectNext(baseApplication)
                .verifyComplete();

        verify(creditApplicationRepository).updateApplicationStatus(id, 2);
        verify(notificationGateway, timeout(200)).sendNotification(anyString());
    }

    @Test
    @DisplayName("updateApplicationStatus (simple) propaga errores amigables")
    void updateStatusSimple_propagatesFriendlyErrors() {
        UUID id = baseApplication.getId();
        when(creditApplicationRepository.updateApplicationStatus(id, 9))
                .thenReturn(Mono.error(new IllegalArgumentException("not found")));

        Mono<CreditApplication> result = useCase.updateApplicationStatus(id, 9);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().contains("Solicitud no encontrada"))
                .verify();
    }

    @Test
    @DisplayName("getCreditApplications une usuarios y calcula monthlyAmount")
    void getCreditApplications_happyPath() {
        String jwt = "token";
        List<CreditApplication> apps = Collections.singletonList(baseApplication);

        when(creditApplicationRepository.getCreditApplications(0, 10, null))
                .thenReturn(Flux.fromIterable(apps));
        when(creditApplicationRepository.countCreditApplications(null))
                .thenReturn(Mono.just(1L));

        UserModel user = new UserModel();
        user.setDocumentNumber("123");
        user.setName("John Doe");
        user.setEmail("user@test.com");
        user.setSalaryBase(BigDecimal.valueOf(2_000_000));

        when(userService.findUsersByDocumentNumber(eq(jwt), any(String[].class)))
                .thenReturn(Mono.just(new UserModel[]{user}));

        StepVerifier.create(useCase.getCreditApplications(jwt, 1, 10, null))
                .assertNext(dto -> {
                    assertThat(dto.getData()).hasSize(1);
                    assertThat(dto.getData().get(0).getName()).isEqualTo("John Doe");
                    assertThat(dto.getMeta().getTotalElements()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("calculateCapacity envía SQS cuando hay validación automática")
    void calculateCapacity_sendsCapacityRequestWhenAutoValidation() {
        String jwt = "t";
        when(userService.findUsersByDocumentNumber(jwt, new String[]{"123"}))
                .thenReturn(Mono.just(new UserModel[]{new UserModel() {{
                    setEmail("user@test.com");
                    setDocumentNumber("123");
                    setSalaryBase(BigDecimal.valueOf(3_000_000));
                }}}));

        when(loanTypeRepository.findById("1"))
                .thenReturn(Mono.just(LoanType.builder().id(1).name("P").interestRate(12.0).build()));
        when(loanTypeRepository.hasAutomaticValidation(1))
                .thenReturn(Mono.just(true));

        CreditApplication saved = CreditApplication.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1_000_000))
                .monthTerm(12)
                .loanType(LoanType.builder().id(1).interestRate(12.0).build())
                .build();

        when(creditApplicationRepository.createCreditApplication(any(CreditApplication.class)))
                .thenReturn(Mono.just(saved));
        when(creditApplicationRepository.getActiveLoansByDocumentNumber("123"))
                .thenReturn(Flux.empty());
        when(loanCapacityGateway.sendLoanCapacityRequest(anyString()))
                .thenReturn(Mono.empty());

        Mono<UUID> result = useCase.calculateCapacity("123", BigDecimal.valueOf(1_000_000), 12, 1, jwt);

        StepVerifier.create(result)
                .assertNext(id -> assertThat(id).isEqualTo(saved.getId()))
                .verifyComplete();

        verify(loanCapacityGateway).sendLoanCapacityRequest(anyString());
    }

    @Test
    @DisplayName("calculateCapacity sin validación automática solo guarda")
    void calculateCapacity_withoutAutoValidation_justSaves() {
        String jwt = "t";
        when(userService.findUsersByDocumentNumber(jwt, new String[]{"123"}))
                .thenReturn(Mono.just(new UserModel[]{new UserModel() {{
                    setEmail("user@test.com");
                    setDocumentNumber("123");
                    setSalaryBase(BigDecimal.valueOf(3_000_000));
                }}}));

        when(loanTypeRepository.findById("1"))
                .thenReturn(Mono.just(LoanType.builder().id(1).name("P").interestRate(12.0).build()));
        when(loanTypeRepository.hasAutomaticValidation(1))
                .thenReturn(Mono.just(false)); // Sin validación automática

        CreditApplication saved = CreditApplication.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1_000_000))
                .monthTerm(12)
                .loanType(LoanType.builder().id(1).interestRate(12.0).build())
                .build();

        when(creditApplicationRepository.createCreditApplication(any(CreditApplication.class)))
                .thenReturn(Mono.just(saved));

        Mono<UUID> result = useCase.calculateCapacity("123", BigDecimal.valueOf(1_000_000), 12, 1, jwt);

        StepVerifier.create(result)
                .assertNext(id -> assertThat(id).isEqualTo(saved.getId()))
                .verifyComplete();

        verify(creditApplicationRepository).createCreditApplication(any(CreditApplication.class));
        verifyNoInteractions(loanCapacityGateway);
    }

    @Test
    @DisplayName("getCreditApplications con filtro de estado")
    void getCreditApplications_withStatusFilter() {
        String jwt = "token";
        List<CreditApplication> apps = Collections.singletonList(baseApplication);

        when(creditApplicationRepository.getCreditApplications(0, 10, "PENDIENTE"))
                .thenReturn(Flux.fromIterable(apps));
        when(creditApplicationRepository.countCreditApplications("PENDIENTE"))
                .thenReturn(Mono.just(1L));

        UserModel user = new UserModel();
        user.setDocumentNumber("123");
        user.setName("John Doe");
        user.setEmail("user@test.com");
        user.setSalaryBase(BigDecimal.valueOf(2_000_000));

        when(userService.findUsersByDocumentNumber(eq(jwt), any(String[].class)))
                .thenReturn(Mono.just(new UserModel[]{user}));

        StepVerifier.create(useCase.getCreditApplications(jwt, 1, 10, "PENDIENTE"))
                .assertNext(dto -> {
                    assertThat(dto.getData()).hasSize(1);
                    assertThat(dto.getData().get(0).getName()).isEqualTo("John Doe");
                    assertThat(dto.getMeta().getTotalElements()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("createCreditApplication delega al helper y retorna resultado")
    void createCreditApplication_delegatesToHelper() {
        CreditApplication input = CreditApplication.builder()
                .amount(BigDecimal.valueOf(1_000_000))
                .monthTerm(12)
                .email("user@test.com")
                .documentNumber("123")
                .loanType(LoanType.builder().id(1).name("PERSONAL").build())
                .build();

        when(userService.validateUser(eq("123"), any()))
                .thenReturn(Mono.just(true));
        when(loanTypeRepository.findByName("PERSONAL"))
                .thenReturn(Mono.just(LoanType.builder().id(1).name("PERSONAL").build()));
        when(creditApplicationRepository.createCreditApplication(any(CreditApplication.class)))
                .thenReturn(Mono.just(input));

        Mono<CreditApplication> result = useCase.createCreditApplication(input, "jwt");

        StepVerifier.create(result)
                .expectNext(input)
                .verifyComplete();

        verify(userService).validateUser(eq("123"), any());
        verify(loanTypeRepository).findByName("PERSONAL");
        verify(creditApplicationRepository).createCreditApplication(any(CreditApplication.class));
    }

    // Tests para MonthlyPaymentCalculator
    @Test
    @DisplayName("MonthlyPaymentCalculator retorna 0 cuando hay nulls o meses <= 0")
    void monthlyPaymentCalculator_returnsZeroOnInvalidInputs() {
        assertThat(MonthlyPaymentCalculator.calculateMonthlyPayment(null, BigDecimal.TEN, 12))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(MonthlyPaymentCalculator.calculateMonthlyPayment(BigDecimal.TEN, null, 12))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(MonthlyPaymentCalculator.calculateMonthlyPayment(BigDecimal.TEN, BigDecimal.TEN, null))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(MonthlyPaymentCalculator.calculateMonthlyPayment(BigDecimal.TEN, BigDecimal.TEN, 0))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(MonthlyPaymentCalculator.calculateMonthlyPayment(BigDecimal.TEN, BigDecimal.TEN, -5))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("MonthlyPaymentCalculator calcula cuota aproximada")
    void monthlyPaymentCalculator_calculatesMonthlyPayment() {
        BigDecimal principal = BigDecimal.valueOf(1_000_000);
        BigDecimal annualRate = BigDecimal.valueOf(12.0);
        int months = 12;

        BigDecimal pmt = MonthlyPaymentCalculator.calculateMonthlyPayment(principal, annualRate, months);

        assertThat(pmt).isNotNull();
        assertThat(pmt.doubleValue()).isBetween(88_000.0, 89_000.0);
    }

    // Tests para CreditApplicationValidatorHelper
    @Test
    @DisplayName("CreditApplicationValidatorHelper valida campos, usuario y loanType y guarda")
    void validatorHelper_validatesAndSaves() {
        CreditApplication input = CreditApplication.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1_000_000))
                .monthTerm(12)
                .email("user@test.com")
                .documentNumber("123")
                .loanType(LoanType.builder().id(1).name("PERSONAL").build())
                .build();

        when(userService.validateUser(eq("123"), any()))
                .thenReturn(Mono.just(true));
        when(loanTypeRepository.findByName("PERSONAL"))
                .thenReturn(Mono.just(LoanType.builder().id(1).name("PERSONAL").build()));
        when(creditApplicationRepository.createCreditApplication(any(CreditApplication.class)))
                .thenReturn(Mono.just(input));

        Mono<CreditApplication> result = CreditApplicationValidatorHelper
                .validateAndSaveCreditApplication(input, "jwt", creditApplicationRepository, userService, loanTypeRepository);

        StepVerifier.create(result)
                .expectNext(input)
                .verifyComplete();

        verify(userService).validateUser(eq("123"), any());
        verify(loanTypeRepository).findByName("PERSONAL");
        verify(creditApplicationRepository).createCreditApplication(any(CreditApplication.class));
    }

    @Test
    @DisplayName("CreditApplicationValidatorHelper falla por campos requeridos")
    void validatorHelper_failsOnRequiredFields() {
        CreditApplication invalid = CreditApplication.builder()
                .amount(BigDecimal.valueOf(1_000_000))
                .monthTerm(12)
                .documentNumber("123")
                .loanType(LoanType.builder().name("PERSONAL").build())
                .build();

        Mono<CreditApplication> result = CreditApplicationValidatorHelper
                .validateAndSaveCreditApplication(invalid, "jwt", creditApplicationRepository, userService, loanTypeRepository);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();

        verifyNoInteractions(userService, loanTypeRepository, creditApplicationRepository);
    }

    @Test
    @DisplayName("CreditApplicationValidatorHelper falla cuando usuario no existe")
    void validatorHelper_failsWhenUserNotFound() {
        CreditApplication input = CreditApplication.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1_000_000))
                .monthTerm(12)
                .email("user@test.com")
                .documentNumber("123")
                .loanType(LoanType.builder().id(1).name("PERSONAL").build())
                .build();

        when(userService.validateUser(eq("123"), any()))
                .thenReturn(Mono.just(false));

        Mono<CreditApplication> result = CreditApplicationValidatorHelper
                .validateAndSaveCreditApplication(input, "jwt", creditApplicationRepository, userService, loanTypeRepository);

        StepVerifier.create(result)
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("CreditApplicationValidatorHelper falla cuando loanType no existe")
    void validatorHelper_failsWhenLoanTypeMissing() {
        CreditApplication input = CreditApplication.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1_000_000))
                .monthTerm(12)
                .email("user@test.com")
                .documentNumber("123")
                .loanType(LoanType.builder().id(1).name("PERSONAL").build())
                .build();

        when(userService.validateUser(eq("123"), any()))
                .thenReturn(Mono.just(true));
        when(loanTypeRepository.findByName("PERSONAL"))
                .thenReturn(Mono.empty());

        Mono<CreditApplication> result = CreditApplicationValidatorHelper
                .validateAndSaveCreditApplication(input, "jwt", creditApplicationRepository, userService, loanTypeRepository);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();
    }
}
