package co.com.crediya.r2dbc;

import co.com.crediya.model.creditapplication.CreditApplication;
import co.com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.requeststate.RequestState;
import co.com.crediya.r2dbc.entities.CreditApplicationEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
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
        CreditApplicationEntity creditApplicationEntity = mapper.map(creditApplication, CreditApplicationEntity.class);
        creditApplicationEntity.setLoanTypeId(creditApplication.getLoanType().getId());
        creditApplicationEntity.setRequestStateId(creditApplication.getRequestState().getId());
        return repository
                .save(creditApplicationEntity)
                .map(creditApplicationEntitySaved -> {
                    CreditApplication creditApplicationSaved = mapper.map(creditApplicationEntitySaved, CreditApplication.class);
                    creditApplicationSaved.setLoanType(LoanType.builder().id(creditApplicationEntitySaved.getLoanTypeId()).build());
                    creditApplicationSaved.setRequestState(RequestState.builder().id(creditApplicationEntitySaved.getRequestStateId()).build());
                    return creditApplicationSaved;
                });
    }
}
