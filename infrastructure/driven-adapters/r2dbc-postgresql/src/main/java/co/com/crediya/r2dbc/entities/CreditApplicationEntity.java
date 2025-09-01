package co.com.crediya.r2dbc.entities;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requeststate.RequestState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit-applications", schema = "applications")
public class CreditApplicationEntity {
    @Id
    @Column("credit_application_id")
    private UUID id;

    private BigDecimal amount;

    @Column("month_term")
    private Integer monthTerm;

    private String email;

    @Column("document_number")
    private String documentNumber;

    @Column("loan_type_id")
    private Integer loanTypeId;

    @Column("request_state_id")
    private Integer requestStateId;
}
