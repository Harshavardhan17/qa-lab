package com.bank.qalab.dto;

import com.bank.qalab.domain.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public final class AccountDtos {

    private AccountDtos() {}

    public record AccountResponse(
            Long id,
            String accountNumber,
            Long customerId,
            String customerName,
            String type,
            String status,
            BigDecimal balance,
            String currency,
            String branchIfsc,
            Instant openedAt
    ) {}

    public record OpenAccountRequest(
            @NotNull AccountType type,
            String currency,
            String branchIfsc,
            @PositiveOrZero BigDecimal initialDeposit,
            /** ADMIN only: open on behalf of another customer. Ignored for CUSTOMER callers. */
            Long customerId
    ) {}

    /** Deposit / withdraw payload. */
    public record MoneyRequest(
            @NotNull @Positive BigDecimal amount,
            String description
    ) {}

    public record UpdateStatusRequest(
            @NotNull com.bank.qalab.domain.AccountStatus status
    ) {}
}
