package com.bank.qalab;

import com.bank.qalab.repo.AccountRepository;
import com.bank.qalab.repo.LoanRepository;
import com.bank.qalab.repo.TransactionRepository;
import com.bank.qalab.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boots the whole Spring context against in-memory H2 and confirms the seeder ran.
 * This validates every JPA mapping, repository query wiring and the security config
 * without needing Docker/Postgres.
 */
@SpringBootTest
@ActiveProfiles("test")
class ContextLoadsTest {

    @Autowired UserRepository users;
    @Autowired AccountRepository accounts;
    @Autowired TransactionRepository transactions;
    @Autowired LoanRepository loans;

    @Test
    void contextLoadsAndSeedsData() {
        assertTrue(users.count() >= 9, "expected admin + 8 customer logins");
        assertTrue(accounts.count() >= 8, "expected at least one account per customer");
        assertTrue(transactions.count() > 0, "expected seeded transaction history");
        assertTrue(loans.count() >= 5, "expected seeded loans");
    }
}
