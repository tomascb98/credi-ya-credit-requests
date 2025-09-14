package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.CreateCreditApplicationRequestDto;
import co.com.crediya.api.dto.CreateCreditApplicationResponseDto;
import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.loantype.LoanType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CreditApplicationDtoMapper {
    @Mapping(target = "loanType", source = "loanTypeName", qualifiedByName = "mapLoanTypeFromName")
    CreditApplication mapToEntity (CreateCreditApplicationRequestDto createCreditApplicationRequestDto);

    @Named("mapLoanTypeFromName")
    default LoanType mapLoanTypeFromName(String loanTypeName) {
        if(loanTypeName == null || loanTypeName.trim().isEmpty()) {
            return null;
        }
        return LoanType.builder()
                .name(loanTypeName.trim())
                .build();
    }
    
    @Mapping(target = "message", constant = "Se creó la solicitud de crédito exitosamente")
    @Mapping(target = "loanTypeName", source = "loanType.name")
    @Mapping(target = "status", source = "requestState.name")
    CreateCreditApplicationResponseDto mapToResponseDto(CreditApplication creditApplication);
}
