package co.com.crediya.r2dbc;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.r2dbc.entities.LoanTypeEntity;
import co.com.crediya.r2dbc.helper.MapperHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LoanTypeReactiveRepositoryAdapter implements LoanTypeRepository {
    
    private final LoanTypeReactiveRepository loanTypeReactiveRepository;
    private final MapperHelper mapperHelper;
    
    @Override
    public Mono<LoanType> findById(String id) {
        return loanTypeReactiveRepository.findById(Integer.valueOf(id))
                .map(mapperHelper::toLoanType);
    }
    
    @Override
    public Mono<Boolean> existsByName(String name) {
        return loanTypeReactiveRepository.existsByName(name);
    }
    
    @Override
    public Mono<LoanType> findByName(String name) {
        log.debug("Buscando tipo de préstamo por nombre: {}", name);
        return loanTypeReactiveRepository.findByName(name)
                .doOnNext(entity -> log.debug("Tipo de préstamo encontrado: name={}", entity.getName()))
                .map(mapperHelper::toLoanType)
                .doOnNext(loanType -> log.info("Tipo de préstamo validado: name={}", loanType.getName()));
    }
}
