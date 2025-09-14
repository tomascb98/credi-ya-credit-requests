package co.com.crediya.model.user.gateways;

import reactor.core.publisher.Mono;

public interface UserService {
    Mono<Boolean> validateUser(String documentNumber, String jwtToken);
}
