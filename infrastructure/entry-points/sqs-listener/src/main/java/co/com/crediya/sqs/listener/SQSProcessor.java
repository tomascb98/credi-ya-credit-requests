package co.com.crediya.sqs.listener;

import co.com.crediya.usecase.creditapplication.CreditApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final CreditApplicationUseCase creditApplicationUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        System.out.println("=== MENSAJE RECIBIDO DE CREDIT-DECISIONS ===");
        System.out.println(message.body());
        System.out.println("=== PROCESANDO DECISIÓN ===");
        
        return Mono.fromCallable(() -> mapCreditDecision(message.body()))
                .flatMap(mapped -> creditApplicationUseCase.updateApplicationStatus(mapped.applicationId, mapped.statusId))
                .doOnSuccess(v -> System.out.println("Decisión procesada exitosamente"))
                .doOnError(e -> System.err.println("Error procesando decisión: " + e.getMessage()))
                .onErrorResume(IllegalArgumentException.class, e -> {
                    System.err.println("Error de validación en mensaje: " + e.getMessage());
                    return Mono.empty(); // Continuar procesando otros mensajes
                })
                .onErrorResume(Exception.class, e -> {
                    System.err.println("Error inesperado procesando decisión: " + e.getMessage());
                    return Mono.empty(); // Continuar procesando otros mensajes
                })
                .then();
    }

    private CreditDecisionMapped mapCreditDecision(String messageBody) {
        if (messageBody == null || messageBody.isBlank()) {
            throw new IllegalArgumentException("Mensaje vacío o nulo");
        }

        // Extraer ID: solicitudId | applicationId
        String idStr = extractValue(messageBody, "solicitudId", "applicationId");
        if (idStr == null || idStr.isBlank()) {
            throw new IllegalArgumentException("El mensaje no contiene 'solicitudId' ni 'applicationId'");
        }

        UUID applicationId;
        try {
            applicationId = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID de solicitud inválido: " + idStr, e);
        }

        // Extraer decisión: resultado | decision
        String decision = extractValue(messageBody, "resultado", "decision");
        int statusId = mapDecisionToStatusId(decision);

        return new CreditDecisionMapped(applicationId, statusId);
    }

    private String extractValue(String json, String... fieldNames) {
        for (String field : fieldNames) {
            String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
            Pattern p = Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    private int mapDecisionToStatusId(String decision) {
        if (decision == null) return 1; // PENDIENTE por defecto
        String normalized = decision.toUpperCase();
        switch (normalized) {
            case "APROBADO":
                return 2;
            case "RECHAZADO":
                return 3;
            default:
                return 1; // PENDIENTE
        }
    }

    private record CreditDecisionMapped(UUID applicationId, int statusId) {}
}
