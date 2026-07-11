package com.bank.qalab.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.time.LocalDate;

public final class CustomerDtos {

    private CustomerDtos() {}

    public record CustomerResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            LocalDate dateOfBirth,
            String addressLine,
            String city,
            String country,
            String status,
            Instant createdAt
    ) {}

    /** Full create (all required fields present). */
    public record CreateCustomerRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            String phone,
            LocalDate dateOfBirth,
            String addressLine,
            String city,
            String country
    ) {}

    /** Full replace via PUT (same shape as create). */
    public record UpdateCustomerRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            String phone,
            LocalDate dateOfBirth,
            String addressLine,
            String city,
            String country
    ) {}

    /** Partial update via PATCH: every field optional; only non-null fields are applied. */
    public record PatchCustomerRequest(
            String firstName,
            String lastName,
            @Email String email,
            String phone,
            LocalDate dateOfBirth,
            String addressLine,
            String city,
            String country,
            String status
    ) {}
}
