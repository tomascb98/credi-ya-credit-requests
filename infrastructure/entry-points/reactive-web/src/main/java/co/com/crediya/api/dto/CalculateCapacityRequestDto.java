package co.com.crediya.api.dto;

import java.math.BigDecimal;

public record CalculateCapacityRequestDto(
        String documentNumber,
        BigDecimal requestedAmount,
        Integer monthTerm,
        Integer loanTypeId
) { }
