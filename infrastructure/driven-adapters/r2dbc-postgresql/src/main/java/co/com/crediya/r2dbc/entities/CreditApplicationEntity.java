package co.com.crediya.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_applications", schema = "credit")
public class CreditApplicationEntity {
    @Id
    @Column("id")
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

    // Campos adicionales para los joins (no se mapean a la tabla principal)
    @Transient
    @Column("lt_id")
    private Integer loanTypeIdFromJoin;

    @Transient
    @Column("lt_name")
    private String loanTypeName;

    @Transient
    @Column("minimum_amount")
    private BigDecimal minimumAmount;

    @Transient
    @Column("maximum_amount")
    private BigDecimal maximumAmount;

    @Transient
    @Column("interest_rate")
    private Double interestRate;

    @Transient
    @Column("automatic_validation")
    private Boolean automaticValidation;

    @Transient
    @Column("rs_id")
    private Integer requestStateIdFromJoin;

    @Transient
    @Column("rs_name")
    private String requestStateName;

    @Transient
    @Column("description")
    private String description;
}
