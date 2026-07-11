package com.bank.qalab.repo;

import com.bank.qalab.domain.Account;
import com.bank.qalab.domain.AccountStatus;
import com.bank.qalab.domain.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    long countByCustomerId(Long customerId);

    @Query("""
            select a from Account a
            where (:customerId is null or a.customer.id = :customerId)
              and (:status is null or a.status = :status)
              and (:type is null or a.type = :type)
            """)
    Page<Account> search(@Param("customerId") Long customerId,
                         @Param("status") AccountStatus status,
                         @Param("type") AccountType type,
                         Pageable pageable);
}
