package com.bank.qalab.web.api;

import com.bank.qalab.domain.TransferStatus;
import com.bank.qalab.dto.PageResponse;
import com.bank.qalab.dto.TransferDtos.CreateTransferRequest;
import com.bank.qalab.dto.TransferDtos.TransferResponse;
import com.bank.qalab.security.AppUserPrincipal;
import com.bank.qalab.service.TransferService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "4. Transfers", description = "Move money between accounts (internal) or to a beneficiary (external)")
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse create(@Valid @RequestBody CreateTransferRequest request,
                                   @AuthenticationPrincipal AppUserPrincipal principal) {
        return transferService.create(request, principal);
    }

    @GetMapping
    public PageResponse<TransferResponse> list(@AuthenticationPrincipal AppUserPrincipal principal,
                                               @RequestParam(required = false) TransferStatus status,
                                               @PageableDefault(size = 20) Pageable pageable) {
        return transferService.list(principal, status, pageable);
    }

    @GetMapping("/{id}")
    public TransferResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        return transferService.get(id, principal);
    }

    /** ADMIN only. */
    @PostMapping("/{id}/reverse")
    public TransferResponse reverse(@PathVariable Long id) {
        return transferService.reverse(id);
    }
}
