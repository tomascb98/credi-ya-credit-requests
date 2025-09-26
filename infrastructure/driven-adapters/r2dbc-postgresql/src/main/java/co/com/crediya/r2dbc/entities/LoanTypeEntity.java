package co.com.crediya.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loan_types", schema = "credit")
public class LoanTypeEntity {
    @Id
    private Integer id;
    
    private String name;
    
    @Column("minimum_amount")
    private BigDecimal minimumAmount;
    
    @Column("maximum_amount")
    private BigDecimal maximumAmount;
    
    @Column("interest_rate")
    private BigDecimal interestRate;
    
    @Column("automatic_validation")
    private Boolean automaticValidation;
    
    @Column("created_at")
    private java.time.LocalDateTime createdAt;
    
    @Column("updated_at")
    private java.time.LocalDateTime updatedAt;
}
