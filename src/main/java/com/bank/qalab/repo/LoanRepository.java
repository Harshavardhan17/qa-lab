package com.bank.qalab.repo;

import com.bank.qalab.domain.Loan;
import com.bank.qalab.domain.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("""
            select l from Loan l
            where (:customerId is null or l.customer.id = :customerId)
              and (:status is null or l.status = :status)
            """)
    Page<Loan> search(@Param("customerId") Long customerId,
                      @Param("status") LoanStatus status,
                      Pageable pageable);
}
