package co.com.crediya.consumer.dto;

public record ValidateUserResponseDto(
        String message,
        Boolean isValid
) {}
