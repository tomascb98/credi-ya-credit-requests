package co.com.crediya.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO para respuestas de error de la API")
public record ErrorResponseDto(
    @Schema(description = "Mensaje descriptivo del error", example = "Los campos email, monto, plazo, numero de documento y tipo de prestamo son obligatorios.")
    String message,
    
    @Schema(description = "Código de error específico", example = "VALIDATION_ERROR")
    String error,
    
    @Schema(description = "Código de estado HTTP", example = "400")
    int status,
    
    @Schema(description = "Timestamp de cuando ocurrió el error", example = "2024-01-15T10:30:45.123456")
    LocalDateTime timestamp
) {
    public static ErrorResponseDto of(String message, String error, int status) {
        return new ErrorResponseDto(message, error, status, LocalDateTime.now());
    }
}
