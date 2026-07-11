package com.bank.qalab.repo;

import com.bank.qalab.domain.Transfer;
import com.bank.qalab.domain.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByReference(String reference);

    @Query("""
            select t from Transfer t
            where (:customerId is null or t.fromAccount.customer.id = :customerId)
              and (:status is null or t.status = :status)
            """)
    Page<Transfer> search(@Param("customerId") Long customerId,
                          @Param("status") TransferStatus status,
                          Pageable pageable);
}
