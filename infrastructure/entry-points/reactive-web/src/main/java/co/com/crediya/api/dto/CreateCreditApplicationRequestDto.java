package co.com.crediya.api.dto;

import java.math.BigDecimal;

public record CreateCreditApplicationRequestDto(
        String documentNumber,
        String email,
        BigDecimal amount,
        Integer monthTerm,
        String loanTypeName
) { }
