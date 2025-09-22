package co.com.crediya.usecase.creditapplication;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.usecase.creditapplication.dto.CreditApplicationWithUserDto;
import co.com.crediya.usecase.creditapplication.dto.PaginatedCreditApplicationsDto;
import co.com.crediya.usecase.creditapplication.dto.PaginationMetaDto;
import co.com.crediya.usecase.creditapplication.helper.MonthlyPaymentCalculator;
import co.com.crediya.model.user.gateways.UserModel;
import co.com.crediya.model.user.gateways.UserService;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.usecase.creditapplication.helper.CreditApplicationValidatorHelper;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.math.BigDecimal;

public class CreditApplicationUseCase {
    private final CreditApplicationRepository creditApplicationRepository;
    private final UserService userService;
    private final LoanTypeRepository loanTypeRepository;

    public CreditApplicationUseCase(CreditApplicationRepository creditApplicationRepository, 
                                   UserService userService,
                                   LoanTypeRepository loanTypeRepository) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.userService = userService;
        this.loanTypeRepository = loanTypeRepository;
    }

    public Mono<CreditApplication> createCreditApplication(CreditApplication creditApplication, String jwtToken) {
        return CreditApplicationValidatorHelper.validateAndSaveCreditApplication(creditApplication, jwtToken, creditApplicationRepository, userService, loanTypeRepository);
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

}
