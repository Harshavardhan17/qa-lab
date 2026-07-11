package com.bank.qalab.dto;

import java.math.BigDecimal;
import java.time.Instant;

public final class TransactionDtos {

    private TransactionDtos() {}

    public record TransactionResponse(
            Long id,
            Long accountId,
            String type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description,
            String counterparty,
            String reference,
            Instant createdAt
    ) {}
}
