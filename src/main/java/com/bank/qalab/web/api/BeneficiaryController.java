package com.bank.qalab.web.api;

import com.bank.qalab.dto.BeneficiaryDtos.BeneficiaryRequest;
import com.bank.qalab.dto.BeneficiaryDtos.BeneficiaryResponse;
import com.bank.qalab.security.AppUserPrincipal;
import com.bank.qalab.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "5. Beneficiaries", description = "Saved payees for the logged-in customer")
@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping
    public List<BeneficiaryResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return beneficiaryService.list(principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficiaryResponse create(@Valid @RequestBody BeneficiaryRequest request,
                                      @AuthenticationPrincipal AppUserPrincipal principal) {
        return beneficiaryService.create(request, principal);
    }

    @GetMapping("/{id}")
    public BeneficiaryResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        return beneficiaryService.get(id, principal);
    }

    @PutMapping("/{id}")
    public BeneficiaryResponse update(@PathVariable Long id,
                                      @Valid @RequestBody BeneficiaryRequest request,
                                      @AuthenticationPrincipal AppUserPrincipal principal) {
        return beneficiaryService.update(id, request, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        beneficiaryService.delete(id, principal);
    }
}
