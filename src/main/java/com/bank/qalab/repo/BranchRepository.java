package com.bank.qalab.repo;

import com.bank.qalab.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByIfsc(String ifsc);
}
