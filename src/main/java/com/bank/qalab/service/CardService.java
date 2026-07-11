package com.bank.qalab.service;

import com.bank.qalab.domain.Account;
import com.bank.qalab.domain.Card;
import com.bank.qalab.domain.CardStatus;
import com.bank.qalab.dto.CardDtos.CardResponse;
import com.bank.qalab.dto.CardDtos.IssueCardRequest;
import com.bank.qalab.dto.CardDtos.UpdateLimitRequest;
import com.bank.qalab.error.NotFoundException;
import com.bank.qalab.repo.AccountRepository;
import com.bank.qalab.repo.CardRepository;
import com.bank.qalab.security.AppUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CardService {

    private final CardRepository cards;
    private final AccountRepository accounts;

    public CardService(CardRepository cards, AccountRepository accounts) {
        this.cards = cards;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<CardResponse> list(AppUserPrincipal actor) {
        Long customerId = Access.requireCustomer(actor);
        return cards.findByAccountCustomerId(customerId).stream().map(Mapper::toCard).toList();
    }

    @Transactional(readOnly = true)
    public CardResponse get(Long id, AppUserPrincipal actor) {
        return Mapper.toCard(findOwned(id, actor));
    }

    public CardResponse issue(IssueCardRequest req, AppUserPrincipal actor) {
        Account account = accounts.findById(req.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found: id=" + req.accountId()));
        Access.requireOwnsAccount(actor, account);
        BigDecimal limit = req.dailyLimit() == null
                ? new BigDecimal("1000.00")
                : req.dailyLimit().setScale(2, RoundingMode.HALF_UP);
        Card card = Card.builder()
                .account(account)
                .cardNumber(Ids.maskedCardNumber())
                .cardHolder(req.cardHolder())
                .type(req.type())
                .status(CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(4).withDayOfMonth(1))
                .dailyLimit(limit)
                .build();
        return Mapper.toCard(cards.save(card));
    }

    public CardResponse block(Long id, AppUserPrincipal actor) {
        Card c = findOwned(id, actor);
        c.setStatus(CardStatus.BLOCKED);
        return Mapper.toCard(cards.save(c));
    }

    public CardResponse updateLimit(Long id, UpdateLimitRequest req, AppUserPrincipal actor) {
        Card c = findOwned(id, actor);
        c.setDailyLimit(req.dailyLimit().setScale(2, RoundingMode.HALF_UP));
        return Mapper.toCard(cards.save(c));
    }

    private Card findOwned(Long id, AppUserPrincipal actor) {
        if (actor.isAdmin()) {
            return cards.findById(id).orElseThrow(() -> new NotFoundException("Card not found: id=" + id));
        }
        Long customerId = Access.requireCustomer(actor);
        return cards.findByIdAndAccountCustomerId(id, customerId)
                .orElseThrow(() -> new NotFoundException("Card not found: id=" + id));
    }
}
