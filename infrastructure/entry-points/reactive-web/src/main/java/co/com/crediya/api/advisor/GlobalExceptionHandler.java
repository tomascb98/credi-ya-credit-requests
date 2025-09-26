package co.com.crediya.api.advisor;

import co.com.crediya.api.dto.ErrorResponseDto;
import co.com.crediya.model.exceptions.AuthenticationException;
import co.com.crediya.model.exceptions.AuthorizationException;
import co.com.crediya.model.exceptions.BusinessRuleException;
import co.com.crediya.model.exceptions.UserNotFoundException;
import co.com.crediya.model.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalExceptionHandler {

    public Mono<ServerResponse> handleValidationException(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .bodyValue(ErrorResponseDto.of(
                        ex.getMessage(),
                        "VALIDATION_ERROR",
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    public Mono<ServerResponse> handleBusinessRuleException(BusinessRuleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.CONFLICT)
                .bodyValue(ErrorResponseDto.of(
                        ex.getMessage(),
                        "BUSINESS_RULE_VIOLATION",
                        HttpStatus.CONFLICT.value()
                ));
    }

    public Mono<ServerResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .bodyValue(ErrorResponseDto.of(
                        ex.getMessage(),
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND.value()
                ));
    }

    public Mono<ServerResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .bodyValue(ErrorResponseDto.of(
                        "Formato de datos inválido",
                        "INVALID_FORMAT",
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    public Mono<ServerResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                .bodyValue(ErrorResponseDto.of(
                        ex.getMessage() != null ? ex.getMessage() : "Token de autenticación inválido o expirado",
                        "UNAUTHORIZED",
                        HttpStatus.UNAUTHORIZED.value()
                ));
    }

    public Mono<ServerResponse> handleAuthorizationException(AuthorizationException ex) {
        log.warn("Authorization error: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.FORBIDDEN)
                .bodyValue(ErrorResponseDto.of(
                        ex.getMessage() != null ? ex.getMessage() : "No tiene permisos para acceder a este recurso",
                        "FORBIDDEN",
                        HttpStatus.FORBIDDEN.value()
                ));
    }

    public Mono<ServerResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue(ErrorResponseDto.of(
                        "Error interno del servidor",
                        "INTERNAL_SERVER_ERROR",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }
}
