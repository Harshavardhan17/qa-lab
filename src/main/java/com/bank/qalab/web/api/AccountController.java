package com.bank.qalab.web.api;

import com.bank.qalab.domain.AccountStatus;
import com.bank.qalab.domain.AccountType;
import com.bank.qalab.dto.AccountDtos.AccountResponse;
import com.bank.qalab.dto.AccountDtos.MoneyRequest;
import com.bank.qalab.dto.AccountDtos.OpenAccountRequest;
import com.bank.qalab.dto.AccountDtos.UpdateStatusRequest;
import com.bank.qalab.dto.PageResponse;
import com.bank.qalab.dto.TransactionDtos.TransactionResponse;
import com.bank.qalab.security.AppUserPrincipal;
import com.bank.qalab.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "3. Accounts", description = "Open accounts, deposit/withdraw, view balances and statements")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /** CUSTOMER sees own accounts; ADMIN sees all. Filter with ?status=ACTIVE&type=SAVINGS */
    @GetMapping
    public PageResponse<AccountResponse> list(@AuthenticationPrincipal AppUserPrincipal principal,
                                              @RequestParam(required = false) AccountStatus status,
                                              @RequestParam(required = false) AccountType type,
                                              @PageableDefault(size = 20) Pageable pageable) {
        return accountService.list(principal, status, type, pageable);
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        return accountService.get(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse open(@Valid @RequestBody OpenAccountRequest request,
                                @AuthenticationPrincipal AppUserPrincipal principal) {
        return accountService.open(request, principal);
    }

    /** Statement. Optional ISO-8601 instants: ?from=2024-01-01T00:00:00Z&to=2024-12-31T23:59:59Z */
    @GetMapping("/{id}/transactions")
    public PageResponse<TransactionResponse> transactions(@PathVariable Long id,
                                                          @RequestParam(required = false) String from,
                                                          @RequestParam(required = false) String to,
                                                          @PageableDefault(size = 20) Pageable pageable,
                                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        return accountService.transactions(id, parseInstant(from), parseInstant(to), pageable, principal);
    }

    @PostMapping("/{id}/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(@PathVariable Long id,
                                       @Valid @RequestBody MoneyRequest request,
                                       @AuthenticationPrincipal AppUserPrincipal principal) {
        return accountService.deposit(id, request, principal);
    }

    @PostMapping("/{id}/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse withdraw(@PathVariable Long id,
                                        @Valid @RequestBody MoneyRequest request,
                                        @AuthenticationPrincipal AppUserPrincipal principal) {
        return accountService.withdraw(id, request, principal);
    }

    @PatchMapping("/{id}/status")
    public AccountResponse updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody UpdateStatusRequest request,
                                        @AuthenticationPrincipal AppUserPrincipal principal) {
        return accountService.updateStatus(id, request.status(), principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void close(@PathVariable Long id) {
        accountService.close(id);
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid instant '" + value + "'. Use ISO-8601 e.g. 2024-01-01T00:00:00Z");
        }
    }
}
