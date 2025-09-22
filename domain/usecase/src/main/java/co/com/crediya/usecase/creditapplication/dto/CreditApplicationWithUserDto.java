package co.com.crediya.usecase.creditapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreditApplicationWithUserDto {
    private BigDecimal amount;
    private Integer monthTerm;
    private String email;
    private String name;
    private String loanType;
    private BigDecimal interestRate;
    private String requestState;
    private BigDecimal salaryBase;
    private BigDecimal monthlyAmount;
}
