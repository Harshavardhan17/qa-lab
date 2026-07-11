package com.bank.qalab.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public final class TransferDtos {

    private TransferDtos() {}

    public record TransferResponse(
            Long id,
            String reference,
            Long fromAccountId,
            String fromAccountNumber,
            Long toAccountId,
            String toAccountNumber,
            Long beneficiaryId,
            String beneficiaryName,
            BigDecimal amount,
            String currency,
            String description,
            String status,
            Instant createdAt,
            Instant completedAt
    ) {}

    /**
     * Provide exactly one destination: an internal {@code toAccountNumber} OR a saved
     * {@code beneficiaryId}. Validated in the service layer.
     */
    public record CreateTransferRequest(
            @NotNull Long fromAccountId,
            String toAccountNumber,
            Long beneficiaryId,
            @NotNull @Positive BigDecimal amount,
            String description
    ) {}
}
