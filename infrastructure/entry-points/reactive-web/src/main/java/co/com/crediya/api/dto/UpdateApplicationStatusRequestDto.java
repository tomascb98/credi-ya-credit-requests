package co.com.crediya.api.dto;

public record UpdateApplicationStatusRequestDto(
        Integer statusId,
        String reason
) { }
