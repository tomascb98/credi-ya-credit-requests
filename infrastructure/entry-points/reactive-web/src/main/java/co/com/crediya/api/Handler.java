package co.com.crediya.api;

import co.com.crediya.api.advisor.GlobalExceptionHandler;
import co.com.crediya.api.dto.CreateCreditApplicationRequestDto;
import co.com.crediya.api.dto.ErrorResponseDto;
import co.com.crediya.api.dto.UpdateApplicationStatusRequestDto;
import co.com.crediya.api.dto.UpdateApplicationStatusResponseDto;
import co.com.crediya.api.dto.CalculateCapacityRequestDto;
import co.com.crediya.api.dto.CalculateCapacityResponseDto;
import co.com.crediya.api.mapper.CreditApplicationDtoMapper;
import co.com.crediya.model.exceptions.AuthenticationException;
import co.com.crediya.model.exceptions.AuthorizationException;
import co.com.crediya.model.exceptions.BusinessRuleException;
import co.com.crediya.model.exceptions.UserNotFoundException;
import co.com.crediya.model.exceptions.ValidationException;
import co.com.crediya.usecase.creditapplication.CreditApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {
    private final CreditApplicationUseCase creditApplicationUseCase;
    private final CreditApplicationDtoMapper mapper;
    private final GlobalExceptionHandler exceptionHandler;


    public Mono<ServerResponse> createCreditApplication(ServerRequest request) {
        log.info("Iniciando creación de solicitud de crédito");
        
        return request.bodyToMono(CreateCreditApplicationRequestDto.class)
                .doOnNext(dto -> log.info("DTO recibido: loanTypeName={}, monthTerm={}", 
                    dto.loanTypeName(), dto.monthTerm()))
                .flatMap(createCreditApplicationRequestDto -> {
                    log.info("Mapeando DTO a entidad de dominio");
                    return creditApplicationUseCase.createCreditApplication(mapper.mapToEntity(createCreditApplicationRequestDto), request.headers().firstHeader(HttpHeaders.AUTHORIZATION));
                })
                .doOnNext(creditApplication -> log.info("Solicitud de crédito creada exitosamente"))
                .map(mapper::mapToResponseDto)
                .doOnNext(response -> log.info("Preparando respuesta HTTP con status: {}", response.status()))
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response))
                .onErrorResume(AuthenticationException.class, exceptionHandler::handleAuthenticationException)
                .onErrorResume(AuthorizationException.class, exceptionHandler::handleAuthorizationException)
                .onErrorResume(ValidationException.class, exceptionHandler::handleValidationException)
                .onErrorResume(BusinessRuleException.class, exceptionHandler::handleBusinessRuleException)
                .onErrorResume(UserNotFoundException.class, exceptionHandler::handleUserNotFoundException)
                .onErrorResume(IllegalArgumentException.class, exceptionHandler::handleIllegalArgumentException)
                .onErrorResume(Exception.class, exceptionHandler::handleGenericException);
    }

    public Mono<ServerResponse> getCreditApplications(ServerRequest request) {
        log.info("Iniciando consulta de solicitudes de crédito");
        
        String jwtToken = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        log.info("Token recibido en Handler: {}", jwtToken != null ? jwtToken.substring(0, Math.min(20, jwtToken.length())) + "..." : "NULL");
        
        return Mono.fromCallable(() -> {
            String requestState = request.queryParam("state").orElse(null);
            int page = Integer.parseInt(request.queryParam("page").orElse("1"));
            int pageSize = Integer.parseInt(request.queryParam("pageSize").orElse("10"));
            
            log.info("Parámetros de consulta: state={}, page={}, pageSize={}", requestState, page, pageSize);
            
            return new QueryParams(requestState, page, pageSize);
        })
        .flatMap(params -> creditApplicationUseCase.getCreditApplications(jwtToken, params.page, params.pageSize, params.requestState))
        .doOnNext(paginatedResponse -> log.info("Consulta completada exitosamente. Elementos encontrados: {}", 
                paginatedResponse.getData() != null ? paginatedResponse.getData().size() : 0))
        .flatMap(paginatedResponse -> ServerResponse.ok().bodyValue(paginatedResponse))
        .onErrorResume(NumberFormatException.class, ex -> {
            log.warn("Error de formato en parámetros de consulta: {}", ex.getMessage());
            return ServerResponse.badRequest()
                    .bodyValue(ErrorResponseDto.of(
                            "Los parámetros 'page' y 'pageSize' deben ser números válidos",
                            "INVALID_NUMBER_FORMAT",
                            HttpStatus.BAD_REQUEST.value()
                    ));
        })
        .onErrorResume(IllegalArgumentException.class, ex -> {
            log.warn("Parámetros inválidos: {}", ex.getMessage());
            return ServerResponse.badRequest()
                    .bodyValue(ErrorResponseDto.of(
                            ex.getMessage(),
                            "INVALID_PARAMETERS",
                            HttpStatus.BAD_REQUEST.value()
                    ));
        })
        .onErrorResume(AuthenticationException.class, exceptionHandler::handleAuthenticationException)
        .onErrorResume(AuthorizationException.class, exceptionHandler::handleAuthorizationException)
        .onErrorResume(ValidationException.class, exceptionHandler::handleValidationException)
        .onErrorResume(BusinessRuleException.class, exceptionHandler::handleBusinessRuleException)
        .onErrorResume(UserNotFoundException.class, exceptionHandler::handleUserNotFoundException)
        .onErrorResume(Exception.class, exceptionHandler::handleGenericException);
    }

    public Mono<ServerResponse> updateApplicationStatus(ServerRequest request) {
        String applicationId = request.pathVariable("id");
        String jwtToken = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        
        log.info("Actualizando estado de solicitud: applicationId={}", applicationId);
        
        return request.bodyToMono(UpdateApplicationStatusRequestDto.class)
                .flatMap(updateRequest -> 
                    creditApplicationUseCase.updateApplicationStatus(
                            UUID.fromString(applicationId), 
                            updateRequest.statusId(), 
                            updateRequest.reason()
                    )
                    .map(updatedApplication -> {
                        // Mapear el estado ID a nombre
                        String statusName = updatedApplication.getRequestState() != null ? 
                                updatedApplication.getRequestState().getName() : "PENDIENTE";
                        
                        return new UpdateApplicationStatusResponseDto(
                                updatedApplication.getId(),
                                "Estado actualizado exitosamente",
                                statusName,
                                updateRequest.reason()
                        );
                    })
                )
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("ID de solicitud inválido: {}", ex.getMessage());
                    return ServerResponse.badRequest()
                            .bodyValue(ErrorResponseDto.of(
                                    "ID de solicitud inválido",
                                    "INVALID_APPLICATION_ID",
                                    HttpStatus.BAD_REQUEST.value()
                            ));
                })
                .onErrorResume(AuthenticationException.class, exceptionHandler::handleAuthenticationException)
                .onErrorResume(AuthorizationException.class, exceptionHandler::handleAuthorizationException)
                .onErrorResume(ValidationException.class, exceptionHandler::handleValidationException)
                .onErrorResume(BusinessRuleException.class, exceptionHandler::handleBusinessRuleException)
                .onErrorResume(UserNotFoundException.class, exceptionHandler::handleUserNotFoundException)
                .onErrorResume(Exception.class, exceptionHandler::handleGenericException);
    }

    public Mono<ServerResponse> calculateCapacity(ServerRequest request) {
        String jwtToken = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        
        log.info("Iniciando cálculo de capacidad de endeudamiento");
        
        return request.bodyToMono(CalculateCapacityRequestDto.class)
                .flatMap(calculateRequest -> 
                    creditApplicationUseCase.calculateCapacity(
                            calculateRequest.documentNumber(),
                            calculateRequest.requestedAmount(),
                            calculateRequest.monthTerm(),
                            calculateRequest.loanTypeId(),
                            jwtToken
                    )
                    .map(applicationId -> {
                        String traceId = "trace-" + UUID.randomUUID().toString().substring(0, 8);
                        return new CalculateCapacityResponseDto(
                                applicationId,
                                "Solicitud de cálculo enviada exitosamente",
                                "PENDIENTE",
                                traceId
                        );
                    })
                )
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("Datos de solicitud inválidos: {}", ex.getMessage());
                    return ServerResponse.badRequest()
                            .bodyValue(ErrorResponseDto.of(
                                    "Datos de solicitud inválidos",
                                    "INVALID_REQUEST_DATA",
                                    HttpStatus.BAD_REQUEST.value()
                            ));
                })
                .onErrorResume(AuthenticationException.class, exceptionHandler::handleAuthenticationException)
                .onErrorResume(AuthorizationException.class, exceptionHandler::handleAuthorizationException)
                .onErrorResume(ValidationException.class, exceptionHandler::handleValidationException)
                .onErrorResume(BusinessRuleException.class, exceptionHandler::handleBusinessRuleException)
                .onErrorResume(UserNotFoundException.class, exceptionHandler::handleUserNotFoundException)
                .onErrorResume(Exception.class, exceptionHandler::handleGenericException);
    }
    
    // Clase interna para encapsular parámetros de consulta
    private static class QueryParams {
        final String requestState;
        final int page;
        final int pageSize;
        
        QueryParams(String requestState, int page, int pageSize) {
            this.requestState = requestState;
            this.page = page;
            this.pageSize = pageSize;
        }
    }
}
