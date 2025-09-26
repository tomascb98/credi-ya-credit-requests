package co.com.crediya.consumer;

import co.com.crediya.consumer.dto.ValidateUserRequestDto;
import co.com.crediya.consumer.dto.ValidateUserResponseDto;
import co.com.crediya.consumer.dto.UserDto;
import co.com.crediya.model.exceptions.AuthenticationException;
import co.com.crediya.model.exceptions.AuthorizationException;
import co.com.crediya.model.user.gateways.UserModel;
import co.com.crediya.model.user.gateways.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Arrays;


@Service
@RequiredArgsConstructor
@Slf4j
public class RestConsumer implements UserService {
    private final WebClient client;

    @Override
    @CircuitBreaker(name = "validateUser")
    public Mono<Boolean> validateUser(String documentNumber, String jwtToken) {
        return client
                .post()
                .uri("/api/v1/auth/validateUser")
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .body(Mono.just(new ValidateUserRequestDto(documentNumber)), ValidateUserRequestDto.class)
                .retrieve()
                .bodyToMono(ValidateUserResponseDto.class)
                .map(ValidateUserResponseDto::isValid)
                .onErrorMap(WebClientResponseException.class, this::handleWebClientException);
    }

    @Override
    @CircuitBreaker(name = "findUsersByDocumentNumber")
    public Mono<UserModel[]> findUsersByDocumentNumber(String jwtToken, String[] documentNumbers) {
        if (documentNumbers == null || documentNumbers.length == 0) {
            return Mono.just(new UserModel[0]);
        }
        
        // Convertir array a string separado por comas para el parámetro de URL
        String documentNumbersParam = String.join(",", documentNumbers);
            
        return client
                .get()
                .uri("/api/v1/auth/usersByDocumentNumbers?documentNumbers=" + documentNumbersParam)
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .retrieve()
                .bodyToMono(UserDto[].class)
                .map(userDtos -> {
                    if (userDtos == null) {
                        return new UserModel[0];
                    }
                    
                    return Arrays.stream(userDtos)
                            .map(this::mapToUserModel)
                            .toArray(UserModel[]::new);
                })
                .onErrorMap(WebClientResponseException.class, this::handleWebClientException)
                .onErrorReturn(throwable -> {
                    log.error("Error no manejado en findUsersByDocumentNumber: {}", throwable.getMessage());
                    return true;
                }, new UserModel[0]);
    }
    
    private UserModel mapToUserModel(UserDto userDto) {
        return UserModel.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .documentNumber(userDto.getDocumentNumber())
                .salaryBase(userDto.getSalaryBase())
                .build();
    }
    
    private Throwable handleWebClientException(WebClientResponseException ex) {
        log.error("Error en llamada a servicio de autenticación: {} - {}", ex.getStatusCode(), ex.getMessage());
        
        if (ex.getStatusCode().value() == 401) {
            return new AuthenticationException("Token de autenticación inválido o expirado", ex);
        } else if (ex.getStatusCode().value() == 403) {
            return new AuthorizationException("No tiene permisos para acceder a este recurso", ex);
        } else {
            return new RuntimeException("Error en servicio de autenticación: " + ex.getMessage(), ex);
        }
    }
}
