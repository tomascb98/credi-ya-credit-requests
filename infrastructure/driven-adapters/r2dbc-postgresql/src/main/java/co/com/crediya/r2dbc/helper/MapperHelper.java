package co.com.crediya.r2dbc.helper;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.r2dbc.entities.LoanTypeEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MapperHelper {
    
    /**
     * Convierte LoanTypeEntity a LoanType
     */
    public LoanType toLoanType(LoanTypeEntity entity) {
        return LoanType.builder()
                .id(entity.getId())
                .name(entity.getName())
                .minimumAmount(entity.getMinimumAmount())
                .maximumAmount(entity.getMaximumAmount())
                .interestRate(entity.getInterestRate().doubleValue())
                .automaticValidation(entity.getAutomaticValidation())
                .build();
    }
    
    /**
     * Convierte LoanType a LoanTypeEntity
     */
    public LoanTypeEntity toLoanTypeEntity(LoanType model) {
        return LoanTypeEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .minimumAmount(model.getMinimumAmount())
                .maximumAmount(model.getMaximumAmount())
                .interestRate(BigDecimal.valueOf(model.getInterestRate()))
                .automaticValidation(model.getAutomaticValidation())
                .build();
    }
}
