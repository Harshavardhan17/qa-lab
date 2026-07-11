package com.bank.qalab.repo;

import com.bank.qalab.domain.Customer;
import com.bank.qalab.domain.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    /**
     * Search customers. {@code pattern} is a lower-cased SQL LIKE pattern built by the service
     * (e.g. {@code %alice%} or {@code %%} to match all) — always non-null so PostgreSQL can infer
     * the parameter type. {@code status} may be null to skip status filtering.
     */
    @Query("""
            select c from Customer c
            where (:status is null or c.status = :status)
              and (lower(c.firstName) like :pattern
                   or lower(c.lastName) like :pattern
                   or lower(c.email)   like :pattern)
            """)
    Page<Customer> search(@Param("status") CustomerStatus status,
                          @Param("pattern") String pattern,
                          Pageable pageable);
}
