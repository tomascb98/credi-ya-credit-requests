package co.com.crediya.r2dbc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplicationWithJoinsDto {
    // Campos básicos de credit_applications
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

    // Campos del JOIN con loan_types
    @Column("lt_id")
    private Integer loanTypeIdFromJoin;
    
    @Column("lt_name")
    private String loanTypeName;
    
    @Column("minimum_amount")
    private BigDecimal minimumAmount;
    
    @Column("maximum_amount")
    private BigDecimal maximumAmount;
    
    @Column("interest_rate")
    private Double interestRate;
    
    @Column("automatic_validation")
    private Boolean automaticValidation;

    // Campos del JOIN con request_states
    @Column("rs_id")
    private Integer requestStateIdFromJoin;
    
    @Column("rs_name")
    private String requestStateName;
    
    private String description;
}
