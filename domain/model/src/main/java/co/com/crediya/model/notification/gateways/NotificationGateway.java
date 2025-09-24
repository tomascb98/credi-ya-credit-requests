package co.com.crediya.model.notification.gateways;

import reactor.core.publisher.Mono;

public interface NotificationGateway {
    Mono<String> sendNotification(String message);
}
