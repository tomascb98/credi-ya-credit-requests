package co.com.crediya.usecase.creditapplication;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requeststate.RequestState;
import co.com.crediya.usecase.creditapplication.dto.CreditApplicationWithUserDto;
import co.com.crediya.usecase.creditapplication.dto.PaginatedCreditApplicationsDto;
import co.com.crediya.usecase.creditapplication.dto.PaginationMetaDto;
import co.com.crediya.usecase.creditapplication.helper.MonthlyPaymentCalculator;
import co.com.crediya.model.user.gateways.UserModel;
import co.com.crediya.model.user.gateways.UserService;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.usecase.creditapplication.helper.CreditApplicationValidatorHelper;
import co.com.crediya.model.notification.gateways.NotificationGateway;
import co.com.crediya.model.loancapacity.gateways.LoanCapacityGateway;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.math.BigDecimal;
import java.util.UUID;

public class CreditApplicationUseCase {
    private final CreditApplicationRepository creditApplicationRepository;
    private final UserService userService;
    private final LoanTypeRepository loanTypeRepository;
    private final NotificationGateway notificationGateway;
    private final LoanCapacityGateway loanCapacityGateway;

    public CreditApplicationUseCase(CreditApplicationRepository creditApplicationRepository, 
                                   UserService userService,
                                   LoanTypeRepository loanTypeRepository,
                                   NotificationGateway notificationGateway,
                                   LoanCapacityGateway loanCapacityGateway) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.userService = userService;
        this.loanTypeRepository = loanTypeRepository;
        this.notificationGateway = notificationGateway;
        this.loanCapacityGateway = loanCapacityGateway;
    }

    public Mono<CreditApplication> createCreditApplication(CreditApplication creditApplication, String jwtToken) {
        return CreditApplicationValidatorHelper.validateAndSaveCreditApplication(creditApplication, jwtToken, creditApplicationRepository, userService, loanTypeRepository);
    }


    public Mono<CreditApplication> updateApplicationStatus(UUID applicationId, Integer statusId, String reason) {
        return creditApplicationRepository.updateApplicationStatus(applicationId, statusId)
                .flatMap(updatedApplication -> {
                    // Enviar notificación a SQS de forma asíncrona (fire-and-forget)
                    sendNotificationToSQS(updatedApplication, statusId, reason)
                            .doOnNext(messageId -> System.out.println("Notificación SQS enviada: " + messageId))
                            .doOnError(error -> System.err.println("Error enviando notificación SQS: " + error.getMessage()))
                            .subscribe(); // Fire-and-forget: no bloquea el flujo principal
                    
                    return Mono.just(updatedApplication);
                });
    }

    private Mono<String> sendNotificationToSQS(CreditApplication application, Integer statusId, String reason) {
        // Crear mensaje de notificación
        String notificationMessage = buildNotificationMessage(application, statusId, reason);
        
        // Enviar notificación usando el gateway
        return notificationGateway.sendNotification(notificationMessage);
    }

    private String buildNotificationMessage(CreditApplication application, Integer statusId, String reason) {
        // Construir mensaje según el contrato de la cola
        String emailParaNotificacion = "tscasas98@gmail.com"; // Email quemado para SES Sandbox
        String decision = statusId == 2 ? "APROBADO" : statusId == 3 ? "RECHAZADO" : "PENDIENTE";
        String eventType = "UKNOWN"; // La Lambda siempre lo convierte a "UKNOWN"
        String traceId = "trace-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        
        return String.format(
                "{\"schemaVersion\":1,\"eventType\":\"%s\",\"applicationId\":\"%s\",\"decision\":\"%s\",\"recipientEmail\":\"%s\",\"reason\":\"%s\",\"timestamp\":\"%s\",\"traceId\":\"%s\"}",
                eventType,
                application.getId(),
                decision,
                emailParaNotificacion,
                reason != null ? reason : "",
                java.time.Instant.now().toString(),
                traceId
        );
    }

    public Mono<PaginatedCreditApplicationsDto> getCreditApplications(String jwtToken, int page, int pageSize, String loanStatus) {
        int skip = pageSize * (page - 1);
        
        return Mono.zip(
            creditApplicationRepository.getCreditApplications(skip, pageSize, loanStatus).collectList(),
            creditApplicationRepository.countCreditApplications(loanStatus)
        )
        .flatMap(tuple -> {
            List<CreditApplication> creditApplications = tuple.getT1();
            Long totalElements = tuple.getT2();
            
            return extractDocumentNumbers(creditApplications)
                    .flatMap(documentNumbers -> userService.findUsersByDocumentNumber(jwtToken, documentNumbers))
                    .defaultIfEmpty(new UserModel[0])
                    .map(userArray -> buildUserMap(userArray))
                    .map(userByDocument -> buildCreditApplicationDtos(creditApplications, userByDocument))
                    .map(data -> buildPaginatedResponse(data, totalElements, page, pageSize));
        });
    }
    
    private Mono<String[]> extractDocumentNumbers(List<CreditApplication> creditApplications) {
        return Mono.fromCallable(() -> 
            creditApplications.stream()
                    .map(CreditApplication::getDocumentNumber)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toArray(String[]::new)
        );
    }
    
    private Map<String, UserModel> buildUserMap(UserModel[] userArray) {
        return Arrays.stream(userArray)
                .filter(Objects::nonNull)
                .filter(u -> u.getDocumentNumber() != null)
                .collect(Collectors.toMap(UserModel::getDocumentNumber, Function.identity(), (a, b) -> a));
    }
    
    private List<CreditApplicationWithUserDto> buildCreditApplicationDtos(
            List<CreditApplication> creditApplications, 
            Map<String, UserModel> userByDocument) {
        
        return creditApplications.stream()
                .map(creditApplication -> mapToDto(creditApplication, userByDocument))
                .collect(Collectors.toList());
    }
    
    private CreditApplicationWithUserDto mapToDto(CreditApplication creditApplication, Map<String, UserModel> userByDocument) {
        UserModel user = creditApplication.getDocumentNumber() == null ? 
            null : userByDocument.get(creditApplication.getDocumentNumber());
        
        BigDecimal monthlyAmount = calculateMonthlyAmount(creditApplication);
        String fullName = extractFullName(user);
        
        return CreditApplicationWithUserDto.builder()
                .amount(creditApplication.getAmount())
                .monthTerm(creditApplication.getMonthTerm())
                .email(creditApplication.getEmail())
                .name(fullName)
                .loanType(creditApplication.getLoanType() != null ? creditApplication.getLoanType().getName() : null)
                .interestRate(creditApplication.getLoanType() != null ? 
                    BigDecimal.valueOf(creditApplication.getLoanType().getInterestRate()) : null)
                .requestState(creditApplication.getRequestState() != null ? 
                    creditApplication.getRequestState().getName() : null)
                .salaryBase(user != null ? user.getSalaryBase() : null)
                .monthlyAmount(monthlyAmount)
                .build();
    }
    
    private BigDecimal calculateMonthlyAmount(CreditApplication creditApplication) {
        if (creditApplication.getLoanType() != null && 
            creditApplication.getLoanType().getInterestRate() != null &&
            creditApplication.getMonthTerm() != null &&
            creditApplication.getAmount() != null) {
            
            BigDecimal interestRate = BigDecimal.valueOf(creditApplication.getLoanType().getInterestRate());
            return MonthlyPaymentCalculator.calculateMonthlyPayment(
                creditApplication.getAmount(),
                interestRate,
                creditApplication.getMonthTerm()
            );
        }
        return BigDecimal.ZERO;
    }
    
    private String extractFullName(UserModel user) {
        return user != null && user.getName() != null ? user.getName() : "";
    }
    
    private PaginatedCreditApplicationsDto buildPaginatedResponse(
            List<CreditApplicationWithUserDto> data, 
            Long totalElements, 
            int page, 
            int pageSize) {
        
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean hasNext = page < totalPages;
        boolean hasPrevious = page > 1;
        
        PaginationMetaDto meta = PaginationMetaDto.builder()
                .page(page)
                .pageSize(pageSize)
                .totalElements(totalElements.intValue())
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
        
        return PaginatedCreditApplicationsDto.builder()
                .data(data)
                .meta(meta)
                .build();
    }

    public Mono<UUID> calculateCapacity(String documentNumber, BigDecimal requestedAmount, 
                                       Integer monthTerm, Integer loanTypeId, String jwtToken) {
        return userService.findUsersByDocumentNumber(jwtToken, new String[]{documentNumber})
                .flatMap(users -> {
                    if (users == null || users.length == 0) {
                        return Mono.error(new RuntimeException("Usuario no encontrado"));
                    }
                    UserModel user = users[0];

                    // Obtener LoanType completo y verificar validación automática en paralelo
                    return loanTypeRepository.findById(String.valueOf(loanTypeId))
                            .zipWith(loanTypeRepository.hasAutomaticValidation(loanTypeId))
                            .flatMap(tuple -> {
                                LoanType loanType = tuple.getT1();
                                Boolean hasAutomaticValidation = tuple.getT2();

                                // Crear solicitud de crédito con LoanType completo
                                CreditApplication creditApplication = CreditApplication.builder()
                                        .documentNumber(documentNumber)
                                        .amount(requestedAmount)
                                        .monthTerm(monthTerm)
                                        .email(user.getEmail())
                                        .loanType(loanType)
                                        .requestState(RequestState.builder().id(1).name("PENDIENTE").build())
                                        .build();

                                // Guardar solicitud
                                return creditApplicationRepository.createCreditApplication(creditApplication)
                                        .flatMap(savedApplication -> {
                                            if (hasAutomaticValidation) {
                                                // Obtener préstamos activos y enviar a SQS
                                                return creditApplicationRepository.getActiveLoansByDocumentNumber(documentNumber)
                                                        .collectList()
                                                        .flatMap(activeLoans -> {
                                                            String message = buildLoanCapacityMessage(savedApplication, user, activeLoans);
                                                            return loanCapacityGateway.sendLoanCapacityRequest(message)
                                                                    .then(Mono.just(savedApplication.getId()));
                                                        });
                                            } else {
                                                // Quedar en PENDIENTE para revisión manual
                                                return Mono.just(savedApplication.getId());
                                            }
                                        });
                            });
                });
    }

    private String buildLoanCapacityMessage(CreditApplication application, UserModel user, 
                                          List<CreditApplication> activeLoans) {
        String traceId = "trace-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Construir JSON de préstamos activos
        String prestamosActivosJson = activeLoans.stream()
                .map(loan -> String.format(
                        "{\"monto\":%s,\"tasaMensual\":%s,\"plazoMeses\":%d}",
                        loan.getAmount(),
                        (loan.getLoanType() != null && loan.getLoanType().getInterestRate() != null)
                                ? BigDecimal.valueOf(loan.getLoanType().getInterestRate())
                                : BigDecimal.ZERO,
                        loan.getMonthTerm()
                ))
                .collect(Collectors.joining(",", "[", "]"));
        
        // Obtener tasa del nuevo préstamo desde LoanType
        BigDecimal nuevaTasaMensual = (application.getLoanType() != null && application.getLoanType().getInterestRate() != null)
                ? BigDecimal.valueOf(application.getLoanType().getInterestRate())
                : BigDecimal.ZERO;
        
        // Construir el mensaje JSON según el nuevo contrato
        String message = String.format(
                "{\"solicitudId\":\"%s\",\"ingresosTotales\":%s,\"prestamosActivos\":%s,\"nuevoPrestamo\":{\"monto\":%s,\"tasaMensual\":%s,\"plazoMeses\":%d},\"recipientEmail\":\"%s\",\"traceId\":\"%s\"}",
                application.getId(),
                user.getSalaryBase(), // ingresosTotales del usuario
                prestamosActivosJson,
                application.getAmount(),
                nuevaTasaMensual,
                application.getMonthTerm(),
                "tscasas98@gmail.com",
                traceId
        );
        
        return message;
    }

    public Mono<CreditApplication> updateApplicationStatus(UUID applicationId, Integer statusId) {
        return creditApplicationRepository.updateApplicationStatus(applicationId, statusId)
                .onErrorResume(IllegalArgumentException.class, e -> 
                    Mono.error(new RuntimeException("Solicitud no encontrada: " + applicationId, e)))
                .onErrorResume(Exception.class, e -> 
                    Mono.error(new RuntimeException("Error actualizando solicitud: " + applicationId, e)));
    }

}
