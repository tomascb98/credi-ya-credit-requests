package co.com.crediya.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCreditApplicationResponseDto(
        UUID id,
        String message,
        String documentNumber,
        BigDecimal amount,
        Integer monthTerm,
        String loanTypeName,
        String status
) { }
