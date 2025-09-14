package co.com.crediya.r2dbc;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requeststate.RequestState;
import co.com.crediya.r2dbc.entities.CreditApplicationEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@Slf4j
@Transactional
public class CreditApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        CreditApplication/* change for domain model */,
        CreditApplicationEntity/* change for adapter model */,
        UUID,
        CreditApplicationReactiveRepository
> implements CreditApplicationRepository {
    public CreditApplicationReactiveRepositoryAdapter(CreditApplicationReactiveRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> mapper.map(d, CreditApplication.class/* change for domain model */));
    }

    @Override
    public Mono<CreditApplication> createCreditApplication(CreditApplication creditApplication) {
        log.info("Iniciando persistencia de solicitud de crédito");
        
        CreditApplicationEntity creditApplicationEntity = mapper.map(creditApplication, CreditApplicationEntity.class);
        creditApplicationEntity.setLoanTypeId(creditApplication.getLoanType().getId());
        creditApplicationEntity.setRequestStateId(creditApplication.getRequestState().getId());
        
        log.debug("Entidad mapeada: loanTypeId={}, requestStateId={}", 
            creditApplicationEntity.getLoanTypeId(), creditApplicationEntity.getRequestStateId());
        
        return repository
                .save(creditApplicationEntity)
                .doOnNext(entity -> log.info("Solicitud de crédito persistida en BD exitosamente"))
                .map(creditApplicationEntitySaved -> {
                    CreditApplication creditApplicationSaved = mapper.map(creditApplicationEntitySaved, CreditApplication.class);
                    creditApplicationSaved.setLoanType(creditApplication.getLoanType());
                    creditApplicationSaved.setRequestState(creditApplication.getRequestState());
                    
                    log.debug("Objeto de dominio reconstruido: loanType={}, requestState={}", 
                        creditApplicationSaved.getLoanType().getName(), 
                        creditApplicationSaved.getRequestState().getName());
                    
                    return creditApplicationSaved;
                });
    }
}
