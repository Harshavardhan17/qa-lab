package com.bank.qalab.dto;

import com.bank.qalab.domain.LoanType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class LoanDtos {

    private LoanDtos() {}

    public record LoanResponse(
            Long id,
            Long customerId,
            String type,
            BigDecimal principal,
            BigDecimal interestRate,
            Integer termMonths,
            BigDecimal emiAmount,
            BigDecimal outstanding,
            String status,
            LocalDate startDate,
            Instant createdAt
    ) {}

    public record LoanApplicationRequest(
            @NotNull LoanType type,
            @NotNull @Positive BigDecimal principal,
            @NotNull @Positive BigDecimal interestRate,
            @NotNull @Min(1) Integer termMonths
    ) {}

    public record RepayRequest(
            @NotNull @Positive BigDecimal amount,
            /** Optional: debit this account for the repayment. */
            Long fromAccountId
    ) {}
}
