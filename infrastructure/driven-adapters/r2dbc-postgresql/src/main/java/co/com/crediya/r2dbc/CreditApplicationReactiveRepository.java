package co.com.crediya.r2dbc;

import co.com.crediya.r2dbc.entities.CreditApplicationEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;


public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<CreditApplicationEntity, UUID>, ReactiveQueryByExampleExecutor<CreditApplicationEntity> {

}
