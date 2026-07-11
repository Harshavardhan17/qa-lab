package com.bank.qalab.repo;

import com.bank.qalab.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Statement for one account within an inclusive time window. {@code from}/{@code to} are always
     * non-null (the service supplies wide defaults) so PostgreSQL can infer the parameter types.
     */
    @Query("""
            select t from Transaction t
            where t.account.id = :accountId
              and t.createdAt >= :from
              and t.createdAt <= :to
            order by t.createdAt desc
            """)
    Page<Transaction> findForAccount(@Param("accountId") Long accountId,
                                     @Param("from") Instant from,
                                     @Param("to") Instant to,
                                     Pageable pageable);
}
