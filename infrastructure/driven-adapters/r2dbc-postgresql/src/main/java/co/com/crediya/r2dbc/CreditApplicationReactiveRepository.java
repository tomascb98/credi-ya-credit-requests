package co.com.crediya.r2dbc;

import co.com.crediya.r2dbc.entities.CreditApplicationEntity;
import co.com.crediya.r2dbc.dto.CreditApplicationWithJoinsDto;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<CreditApplicationEntity, UUID>, ReactiveQueryByExampleExecutor<CreditApplicationEntity> {

    @Query("""
        SELECT 
          ca.id, ca.amount, ca.month_term, ca.email, ca.document_number,
          ca.loan_type_id, ca.request_state_id,
          lt.id AS lt_id, lt.name AS lt_name, lt.minimum_amount, lt.maximum_amount,
          lt.interest_rate, lt.automatic_validation,
          rs.id AS rs_id, rs.name AS rs_name, rs.description
        FROM credit.credit_applications ca
        INNER JOIN credit.loan_types lt ON ca.loan_type_id = lt.id
        INNER JOIN credit.request_states rs ON ca.request_state_id = rs.id
        WHERE ($3 IS NULL OR rs.name = $3)
        ORDER BY ca.created_at DESC 
        LIMIT $2 OFFSET $1
        """)
    Flux<CreditApplicationWithJoinsDto> findCreditApplicationsWithJoins(int skip, int pageSize, String filter);

    @Query("""
        SELECT COUNT(*) FROM credit.credit_applications ca
        INNER JOIN credit.request_states rs ON ca.request_state_id = rs.id
        WHERE ($1 IS NULL OR rs.name = $1)
        AND ($1 IS NULL OR $1 != 'APROBADA' OR rs.name != 'APROBADA')
        """)
    Mono<Long> countCreditApplicationsWithFilter(String filter);
}
