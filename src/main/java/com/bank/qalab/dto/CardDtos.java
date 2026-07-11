package com.bank.qalab.dto;

import com.bank.qalab.domain.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class CardDtos {

    private CardDtos() {}

    public record CardResponse(
            Long id,
            Long accountId,
            String accountNumber,
            String cardNumber,
            String cardHolder,
            String type,
            String status,
            LocalDate expiryDate,
            BigDecimal dailyLimit,
            Instant createdAt
    ) {}

    public record IssueCardRequest(
            @NotNull Long accountId,
            @NotNull CardType type,
            @NotBlank String cardHolder,
            @Positive BigDecimal dailyLimit
    ) {}

    public record UpdateLimitRequest(
            @NotNull @Positive BigDecimal dailyLimit
    ) {}
}
