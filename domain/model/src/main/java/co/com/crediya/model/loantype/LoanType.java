package co.com.crediya.model.loantype;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {
    Integer id;
    String name;
    BigDecimal minimumAmount;
    BigDecimal maximumAmount;
    Double interestRate;
    Boolean automaticValidation;
}
