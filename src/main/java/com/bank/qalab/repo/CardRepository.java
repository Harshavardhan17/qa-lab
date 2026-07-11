package com.bank.qalab.repo;

import com.bank.qalab.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByAccountCustomerId(Long customerId);

    Optional<Card> findByIdAndAccountCustomerId(Long id, Long customerId);
}
