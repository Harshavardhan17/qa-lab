package com.bank.qalab.web.api;

import com.bank.qalab.dto.AuthDtos.LoginRequest;
import com.bank.qalab.dto.AuthDtos.MeResponse;
import com.bank.qalab.dto.AuthDtos.RefreshRequest;
import com.bank.qalab.dto.AuthDtos.RegisterRequest;
import com.bank.qalab.dto.AuthDtos.TokenResponse;
import com.bank.qalab.security.AppUserPrincipal;
import com.bank.qalab.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "1. Auth", description = "Register, log in, refresh tokens and read the current user")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return authService.me(principal);
    }
}
