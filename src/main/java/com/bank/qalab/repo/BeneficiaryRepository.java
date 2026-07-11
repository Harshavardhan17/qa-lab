package com.bank.qalab.repo;

import com.bank.qalab.domain.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByCustomerIdOrderByNameAsc(Long customerId);

    Optional<Beneficiary> findByIdAndCustomerId(Long id, Long customerId);
}
