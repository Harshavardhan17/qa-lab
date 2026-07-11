package com.bank.qalab.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request/response payloads for the authentication endpoints. */
public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotBlank String firstName,
            @NotBlank String lastName,
            String phone
    ) {}

    public record LoginRequest(
            @NotBlank String usernameOrEmail,
            @NotBlank String password
    ) {}

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}

    public record TokenResponse(
            String tokenType,
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            String username,
            String role
    ) {}

    public record MeResponse(
            Long userId,
            String username,
            String email,
            String role,
            Long customerId,
            String customerName
    ) {}
}
