package com.bank.qalab.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanServiceTest {

    @Test
    void zeroInterestEmiIsPrincipalDividedByMonths() {
        assertEquals(new BigDecimal("100.00"),
                LoanService.computeEmi(new BigDecimal("1200"), BigDecimal.ZERO, 12));
    }

    @Test
    void standardEmiMatchesAmortisingFormula() {
        // 10,000 @ 12% p.a. over 12 months = 888.49 EMI
        assertEquals(new BigDecimal("888.49"),
                LoanService.computeEmi(new BigDecimal("10000"), new BigDecimal("12.000"), 12));
    }
}
