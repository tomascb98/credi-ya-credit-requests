package co.com.crediya.sqs.sender;

import co.com.crediya.model.notification.gateways.NotificationGateway;
import co.com.crediya.model.loancapacity.gateways.LoanCapacityGateway;
import co.com.crediya.sqs.sender.config.SQSSenderProperties;
import co.com.crediya.sqs.sender.config.LoanCapacitySQSProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationGateway, LoanCapacityGateway {
    private final SQSSenderProperties properties;
    private final LoanCapacitySQSProperties loanCapacityProperties;
    private final SqsAsyncClient client;

    @Override
    public Mono<String> sendNotification(String message) {
        return sendMessage(message, properties.queueUrl(), "notifications");
    }

    @Override
    public Mono<String> sendLoanCapacityRequest(String message) {
        return sendMessage(message, loanCapacityProperties.getQueueUrl(), "loan_capacity");
    }

    private Mono<String> sendMessage(String message, String queueUrl, String queueType) {
        return Mono.fromCallable(() -> buildRequest(message, queueUrl))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Mensaje {} enviado exitosamente: {}", queueType, response.messageId()))
                .doOnError(error -> log.error("Error enviando mensaje a {}: {}", queueType, error.getMessage(), error))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message, String queueUrl) {
        return SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
    }
}
