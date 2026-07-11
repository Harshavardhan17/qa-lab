package com.bank.qalab.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class BeneficiaryDtos {

    private BeneficiaryDtos() {}

    public record BeneficiaryResponse(
            Long id,
            Long customerId,
            String name,
            String nickname,
            String bankName,
            String accountNumber,
            String ifsc,
            Instant createdAt
    ) {}

    /** Used by both POST (create) and PUT (full replace). */
    public record BeneficiaryRequest(
            @NotBlank String name,
            String nickname,
            String bankName,
            @NotBlank String accountNumber,
            String ifsc
    ) {}
}
