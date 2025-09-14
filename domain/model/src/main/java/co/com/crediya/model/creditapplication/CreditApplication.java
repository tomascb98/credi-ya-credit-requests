package co.com.crediya.model.creditapplication;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requeststate.RequestState;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class CreditApplication {
    UUID id;
    BigDecimal amount;
    Integer monthTerm;
    String email;
    String documentNumber;
    LoanType loanType;
    RequestState requestState;
}
