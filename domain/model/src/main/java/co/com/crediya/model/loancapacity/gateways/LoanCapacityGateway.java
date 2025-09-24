package co.com.crediya.model.loancapacity.gateways;

import reactor.core.publisher.Mono;

public interface LoanCapacityGateway {
    Mono<String> sendLoanCapacityRequest(String message);
}