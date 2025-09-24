package co.com.crediya.api.dto;

import java.util.UUID;

public record UpdateApplicationStatusResponseDto(
        UUID applicationId,
        String message,
        String status,
        String reason
) { }
