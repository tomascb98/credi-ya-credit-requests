package co.com.crediya.model.creditapplication;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requeststate.RequestState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreditApplicationTest {

    private UUID testId;
    private BigDecimal testAmount;
    private Integer testMonthTerm;
    private String testEmail;
    private String testDocumentNumber;
    private LoanType testLoanType;
    private RequestState testRequestState;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testAmount = BigDecimal.valueOf(5000000);
        testMonthTerm = 24;
        testEmail = "test@example.com";
        testDocumentNumber = "12345678";
        testLoanType = LoanType.builder()
            .id(1)
            .name("PERSONAL")
            .minimumAmount(BigDecimal.valueOf(1000000))
            .maximumAmount(BigDecimal.valueOf(50000000))
            .build();
        testRequestState = RequestState.builder()
            .id(1)
            .name("PENDIENTE")
            .description("Solicitud en revisión")
            .build();
    }

    @Test
    @DisplayName("Debería crear CreditApplication con builder")
    void shouldCreateCreditApplicationWithBuilder() {
        // Act
        CreditApplication creditApplication = CreditApplication.builder()
            .id(testId)
            .amount(testAmount)
            .monthTerm(testMonthTerm)
            .email(testEmail)
            .documentNumber(testDocumentNumber)
            .loanType(testLoanType)
            .requestState(testRequestState)
            .build();

        // Assert
        assertThat(creditApplication).isNotNull();
        assertThat(creditApplication.getId()).isEqualTo(testId);
        assertThat(creditApplication.getAmount()).isEqualTo(testAmount);
        assertThat(creditApplication.getMonthTerm()).isEqualTo(testMonthTerm);
        assertThat(creditApplication.getEmail()).isEqualTo(testEmail);
        assertThat(creditApplication.getDocumentNumber()).isEqualTo(testDocumentNumber);
        assertThat(creditApplication.getLoanType()).isEqualTo(testLoanType);
        assertThat(creditApplication.getRequestState()).isEqualTo(testRequestState);
    }

    @Test
    @DisplayName("Debería crear CreditApplication con constructor por defecto")
    void shouldCreateCreditApplicationWithDefaultConstructor() {
        // Act
        CreditApplication creditApplication = new CreditApplication();

        // Assert
        assertThat(creditApplication).isNotNull();
        assertThat(creditApplication.getId()).isNull();
        assertThat(creditApplication.getAmount()).isNull();
        assertThat(creditApplication.getMonthTerm()).isNull();
        assertThat(creditApplication.getEmail()).isNull();
        assertThat(creditApplication.getDocumentNumber()).isNull();
        assertThat(creditApplication.getLoanType()).isNull();
        assertThat(creditApplication.getRequestState()).isNull();
    }

    @Test
    @DisplayName("Debería crear CreditApplication con constructor con parámetros")
    void shouldCreateCreditApplicationWithParameterizedConstructor() {
        // Act
        CreditApplication creditApplication = new CreditApplication(
            testId, testAmount, testMonthTerm, testEmail, 
            testDocumentNumber, testLoanType, testRequestState
        );

        // Assert
        assertThat(creditApplication).isNotNull();
        assertThat(creditApplication.getId()).isEqualTo(testId);
        assertThat(creditApplication.getAmount()).isEqualTo(testAmount);
        assertThat(creditApplication.getMonthTerm()).isEqualTo(testMonthTerm);
        assertThat(creditApplication.getEmail()).isEqualTo(testEmail);
        assertThat(creditApplication.getDocumentNumber()).isEqualTo(testDocumentNumber);
        assertThat(creditApplication.getLoanType()).isEqualTo(testLoanType);
        assertThat(creditApplication.getRequestState()).isEqualTo(testRequestState);
    }

    @Test
    @DisplayName("Debería permitir modificar campos con setters")
    void shouldAllowModifyingFieldsWithSetters() {
        // Arrange
        CreditApplication creditApplication = new CreditApplication();
        UUID newId = UUID.randomUUID();
        BigDecimal newAmount = BigDecimal.valueOf(10000000);
        Integer newMonthTerm = 36;
        String newEmail = "new@example.com";
        String newDocumentNumber = "87654321";

        // Act
        creditApplication.setId(newId);
        creditApplication.setAmount(newAmount);
        creditApplication.setMonthTerm(newMonthTerm);
        creditApplication.setEmail(newEmail);
        creditApplication.setDocumentNumber(newDocumentNumber);
        creditApplication.setLoanType(testLoanType);
        creditApplication.setRequestState(testRequestState);

        // Assert
        assertThat(creditApplication.getId()).isEqualTo(newId);
        assertThat(creditApplication.getAmount()).isEqualTo(newAmount);
        assertThat(creditApplication.getMonthTerm()).isEqualTo(newMonthTerm);
        assertThat(creditApplication.getEmail()).isEqualTo(newEmail);
        assertThat(creditApplication.getDocumentNumber()).isEqualTo(newDocumentNumber);
        assertThat(creditApplication.getLoanType()).isEqualTo(testLoanType);
        assertThat(creditApplication.getRequestState()).isEqualTo(testRequestState);
    }

    @Test
    @DisplayName("Debería crear copia con toBuilder")
    void shouldCreateCopyWithToBuilder() {
        // Arrange
        CreditApplication original = CreditApplication.builder()
            .id(testId)
            .amount(testAmount)
            .monthTerm(testMonthTerm)
            .email(testEmail)
            .documentNumber(testDocumentNumber)
            .loanType(testLoanType)
            .requestState(testRequestState)
            .build();

        // Act
        CreditApplication copy = original.toBuilder()
            .amount(BigDecimal.valueOf(10000000))
            .monthTerm(36)
            .build();

        // Assert
        assertThat(copy).isNotSameAs(original);
        assertThat(copy.getId()).isEqualTo(original.getId());
        assertThat(copy.getAmount()).isEqualTo(BigDecimal.valueOf(10000000));
        assertThat(copy.getMonthTerm()).isEqualTo(36);
        assertThat(copy.getEmail()).isEqualTo(original.getEmail());
        assertThat(copy.getDocumentNumber()).isEqualTo(original.getDocumentNumber());
        assertThat(copy.getLoanType()).isEqualTo(original.getLoanType());
        assertThat(copy.getRequestState()).isEqualTo(original.getRequestState());
    }

    @Test
    @DisplayName("Debería manejar valores null correctamente")
    void shouldHandleNullValuesCorrectly() {
        // Act
        CreditApplication creditApplication = CreditApplication.builder()
            .id(null)
            .amount(null)
            .monthTerm(null)
            .email(null)
            .documentNumber(null)
            .loanType(null)
            .requestState(null)
            .build();

        // Assert
        assertThat(creditApplication).isNotNull();
        assertThat(creditApplication.getId()).isNull();
        assertThat(creditApplication.getAmount()).isNull();
        assertThat(creditApplication.getMonthTerm()).isNull();
        assertThat(creditApplication.getEmail()).isNull();
        assertThat(creditApplication.getDocumentNumber()).isNull();
        assertThat(creditApplication.getLoanType()).isNull();
        assertThat(creditApplication.getRequestState()).isNull();
    }

    @Test
    @DisplayName("Debería ser igual a otra instancia con los mismos valores")
    void shouldBeEqualToAnotherInstanceWithSameValues() {
        // Arrange
        CreditApplication creditApplication1 = CreditApplication.builder()
            .id(testId)
            .amount(testAmount)
            .monthTerm(testMonthTerm)
            .email(testEmail)
            .documentNumber(testDocumentNumber)
            .loanType(testLoanType)
            .requestState(testRequestState)
            .build();

        CreditApplication creditApplication2 = CreditApplication.builder()
            .id(testId)
            .amount(testAmount)
            .monthTerm(testMonthTerm)
            .email(testEmail)
            .documentNumber(testDocumentNumber)
            .loanType(testLoanType)
            .requestState(testRequestState)
            .build();

        // Assert
        assertThat(creditApplication1).isEqualTo(creditApplication2);
        assertThat(creditApplication1.hashCode()).isEqualTo(creditApplication2.hashCode());
    }

    @Test
    @DisplayName("Debería tener representación de string correcta")
    void shouldHaveCorrectStringRepresentation() {
        // Arrange
        CreditApplication creditApplication = CreditApplication.builder()
            .id(testId)
            .amount(testAmount)
            .monthTerm(testMonthTerm)
            .email(testEmail)
            .documentNumber(testDocumentNumber)
            .loanType(testLoanType)
            .requestState(testRequestState)
            .build();

        // Act
        String stringRepresentation = creditApplication.toString();

        // Assert
        assertThat(stringRepresentation).isNotNull();
        assertThat(stringRepresentation).contains(testId.toString());
        assertThat(stringRepresentation).contains(testAmount.toString());
        assertThat(stringRepresentation).contains(testMonthTerm.toString());
        assertThat(stringRepresentation).contains(testEmail);
        assertThat(stringRepresentation).contains(testDocumentNumber);
    }
}
