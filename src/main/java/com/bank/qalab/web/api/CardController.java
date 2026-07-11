package com.bank.qalab.web.api;

import com.bank.qalab.dto.CardDtos.CardResponse;
import com.bank.qalab.dto.CardDtos.IssueCardRequest;
import com.bank.qalab.dto.CardDtos.UpdateLimitRequest;
import com.bank.qalab.security.AppUserPrincipal;
import com.bank.qalab.service.CardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "6. Cards", description = "Debit/credit cards linked to the customer's accounts")
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public List<CardResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return cardService.list(principal);
    }

    @GetMapping("/{id}")
    public CardResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        return cardService.get(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse issue(@Valid @RequestBody IssueCardRequest request,
                              @AuthenticationPrincipal AppUserPrincipal principal) {
        return cardService.issue(request, principal);
    }

    @PatchMapping("/{id}/block")
    public CardResponse block(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        return cardService.block(id, principal);
    }

    @PatchMapping("/{id}/limit")
    public CardResponse updateLimit(@PathVariable Long id,
                                    @Valid @RequestBody UpdateLimitRequest request,
                                    @AuthenticationPrincipal AppUserPrincipal principal) {
        return cardService.updateLimit(id, request, principal);
    }
}
