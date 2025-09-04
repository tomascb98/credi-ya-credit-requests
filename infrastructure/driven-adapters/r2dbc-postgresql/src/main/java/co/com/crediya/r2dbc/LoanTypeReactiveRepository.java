package co.com.crediya.r2dbc;

import co.com.crediya.r2dbc.entities.LoanTypeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface LoanTypeReactiveRepository extends ReactiveCrudRepository<LoanTypeEntity, Integer> {
    
    /**
     * Busca un tipo de préstamo por ID
     */
    Mono<LoanTypeEntity> findById(Integer id);
    
    /**
     * Verifica si existe un tipo de préstamo por nombre
     */
    Mono<Boolean> existsByName(String name);
    
    /**
     * Busca un tipo de préstamo por nombre
     */
    Mono<LoanTypeEntity> findByName(String name);
}
