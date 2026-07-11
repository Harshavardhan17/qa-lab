package com.bank.qalab.web.api;

import com.bank.qalab.domain.LoanStatus;
import com.bank.qalab.dto.LoanDtos.LoanApplicationRequest;
import com.bank.qalab.dto.LoanDtos.LoanResponse;
import com.bank.qalab.dto.LoanDtos.RepayRequest;
import com.bank.qalab.dto.PageResponse;
import com.bank.qalab.security.AppUserPrincipal;
import com.bank.qalab.service.LoanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "7. Loans", description = "Apply, approve/reject (ADMIN) and repay loans")
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public PageResponse<LoanResponse> list(@AuthenticationPrincipal AppUserPrincipal principal,
                                           @RequestParam(required = false) LoanStatus status,
                                           @PageableDefault(size = 20) Pageable pageable) {
        return loanService.list(principal, status, pageable);
    }

    @GetMapping("/{id}")
    public LoanResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        return loanService.get(id, principal);
    }

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse apply(@Valid @RequestBody LoanApplicationRequest request,
                              @AuthenticationPrincipal AppUserPrincipal principal) {
        return loanService.apply(request, principal);
    }

    /** ADMIN only. */
    @PostMapping("/{id}/approve")
    public LoanResponse approve(@PathVariable Long id) {
        return loanService.approve(id);
    }

    /** ADMIN only. */
    @PostMapping("/{id}/reject")
    public LoanResponse reject(@PathVariable Long id) {
        return loanService.reject(id);
    }

    @PostMapping("/{id}/repay")
    public LoanResponse repay(@PathVariable Long id,
                              @Valid @RequestBody RepayRequest request,
                              @AuthenticationPrincipal AppUserPrincipal principal) {
        return loanService.repay(id, request, principal);
    }
}
