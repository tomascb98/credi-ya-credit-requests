package co.com.crediya.consumer;

import co.com.crediya.consumer.dto.ValidateUserRequestDto;
import co.com.crediya.consumer.dto.ValidateUserResponseDto;
import co.com.crediya.model.user.gateways.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
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
                .map(ValidateUserResponseDto::isValid);
    }
}
