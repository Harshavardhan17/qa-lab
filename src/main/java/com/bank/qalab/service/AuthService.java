package com.bank.qalab.service;

import com.bank.qalab.domain.AppUser;
import com.bank.qalab.domain.Customer;
import com.bank.qalab.domain.CustomerStatus;
import com.bank.qalab.domain.Role;
import com.bank.qalab.dto.AuthDtos.LoginRequest;
import com.bank.qalab.dto.AuthDtos.MeResponse;
import com.bank.qalab.dto.AuthDtos.RefreshRequest;
import com.bank.qalab.dto.AuthDtos.RegisterRequest;
import com.bank.qalab.dto.AuthDtos.TokenResponse;
import com.bank.qalab.error.ConflictException;
import com.bank.qalab.error.NotFoundException;
import com.bank.qalab.repo.CustomerRepository;
import com.bank.qalab.repo.UserRepository;
import com.bank.qalab.security.AppUserPrincipal;
import com.bank.qalab.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository users;
    private final CustomerRepository customers;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthService(UserRepository users, CustomerRepository customers, PasswordEncoder encoder,
                       AuthenticationManager authManager, JwtService jwt) {
        this.users = users;
        this.customers = customers;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    /** Self-registration: creates a customer profile + a CUSTOMER login and returns tokens. */
    public TokenResponse register(RegisterRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken: " + req.username());
        }
        if (users.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered: " + req.email());
        }
        Customer customer = Customer.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .phone(req.phone())
                .status(CustomerStatus.ACTIVE)
                .build();
        customers.save(customer);

        AppUser user = AppUser.builder()
                .username(req.username())
                .email(req.email())
                .password(encoder.encode(req.password()))
                .role(Role.CUSTOMER)
                .enabled(true)
                .customer(customer)
                .build();
        users.save(user);

        return issueTokens(user.getUsername(), user.getRole().name());
    }

    public TokenResponse login(LoginRequest req) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.usernameOrEmail(), req.password()));
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return issueTokens(principal.getUsername(), principal.getRole());
    }

    public TokenResponse refresh(RefreshRequest req) {
        try {
            Claims claims = jwt.parse(req.refreshToken());
            if (!jwt.isType(claims, JwtService.TYPE_REFRESH)) {
                throw new BadCredentialsException("Provided token is not a refresh token.");
            }
            AppUser user = users.findByUsername(claims.getSubject())
                    .orElseThrow(() -> new BadCredentialsException("Unknown user in token."));
            return issueTokens(user.getUsername(), user.getRole().name());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid or expired refresh token.");
        }
    }

    @Transactional(readOnly = true)
    public MeResponse me(AppUserPrincipal actor) {
        AppUser user = users.findById(actor.getId())
                .orElseThrow(() -> new NotFoundException("User not found."));
        Long customerId = null;
        String customerName = null;
        if (user.getCustomer() != null) {
            customerId = user.getCustomer().getId();
            customerName = user.getCustomer().getFirstName() + " " + user.getCustomer().getLastName();
        }
        return new MeResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().name(), customerId, customerName);
    }

    private TokenResponse issueTokens(String username, String role) {
        String access = jwt.generateAccessToken(username, role);
        String refresh = jwt.generateRefreshToken(username);
        return new TokenResponse("Bearer", access, refresh, jwt.accessTokenSeconds(), username, role);
    }
}
