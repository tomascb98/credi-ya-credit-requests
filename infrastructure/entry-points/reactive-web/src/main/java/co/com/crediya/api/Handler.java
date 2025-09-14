package co.com.crediya.api;

import co.com.crediya.api.advisor.GlobalExceptionHandler;
import co.com.crediya.api.dto.CreateCreditApplicationRequestDto;
import co.com.crediya.api.dto.CreateCreditApplicationResponseDto;
import co.com.crediya.api.mapper.CreditApplicationDtoMapper;
import co.com.crediya.model.creditapplication.CreditApplication;
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
                .onErrorResume(ValidationException.class, exceptionHandler::handleValidationException)
                .onErrorResume(BusinessRuleException.class, exceptionHandler::handleBusinessRuleException)
                .onErrorResume(UserNotFoundException.class, exceptionHandler::handleUserNotFoundException)
                .onErrorResume(IllegalArgumentException.class, exceptionHandler::handleIllegalArgumentException)
                .onErrorResume(Exception.class, exceptionHandler::handleGenericException);
    }
}
