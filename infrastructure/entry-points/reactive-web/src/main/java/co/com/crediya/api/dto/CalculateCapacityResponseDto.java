package co.com.crediya.api.dto;

import java.util.UUID;

public record CalculateCapacityResponseDto(
        UUID applicationId,
        String message,
        String status,
        String traceId
) { }
