package com.bank.qalab.service;

import com.bank.qalab.domain.Account;
import com.bank.qalab.domain.AccountStatus;
import com.bank.qalab.domain.Beneficiary;
import com.bank.qalab.domain.Transaction;
import com.bank.qalab.domain.Transfer;
import com.bank.qalab.domain.TransferStatus;
import com.bank.qalab.domain.TxnType;
import com.bank.qalab.dto.PageResponse;
import com.bank.qalab.dto.TransferDtos.CreateTransferRequest;
import com.bank.qalab.dto.TransferDtos.TransferResponse;
import com.bank.qalab.error.BusinessException;
import com.bank.qalab.error.NotFoundException;
import com.bank.qalab.repo.AccountRepository;
import com.bank.qalab.repo.BeneficiaryRepository;
import com.bank.qalab.repo.TransactionRepository;
import com.bank.qalab.repo.TransferRepository;
import com.bank.qalab.security.AppUserPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@Transactional
public class TransferService {

    private final TransferRepository transfers;
    private final AccountRepository accounts;
    private final BeneficiaryRepository beneficiaries;
    private final TransactionRepository transactions;

    public TransferService(TransferRepository transfers, AccountRepository accounts,
                           BeneficiaryRepository beneficiaries, TransactionRepository transactions) {
        this.transfers = transfers;
        this.accounts = accounts;
        this.beneficiaries = beneficiaries;
        this.transactions = transactions;
    }

    public TransferResponse create(CreateTransferRequest req, AppUserPrincipal actor) {
        Account from = accounts.findById(req.fromAccountId())
                .orElseThrow(() -> new NotFoundException("Source account not found: id=" + req.fromAccountId()));
        Access.requireOwnsAccount(actor, from);
        if (from.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Source account is " + from.getStatus() + ".");
        }

        boolean internal = req.toAccountNumber() != null && !req.toAccountNumber().isBlank();
        boolean external = req.beneficiaryId() != null;
        if (internal == external) {
            throw new BusinessException(
                    "Provide exactly one destination: 'toAccountNumber' (internal) OR 'beneficiaryId' (external).");
        }

        BigDecimal amount = req.amount().setScale(2, RoundingMode.HALF_UP);
        if (from.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds. Balance=" + from.getBalance() + ", requested=" + amount);
        }

        Instant now = Instant.now();
        String reference = uniqueReference();

        Transfer.TransferBuilder builder = Transfer.builder()
                .reference(reference)
                .fromAccount(from)
                .amount(amount)
                .currency(from.getCurrency())
                .description(req.description())
                .status(TransferStatus.COMPLETED)
                .createdAt(now)
                .completedAt(now);

        from.setBalance(from.getBalance().subtract(amount));

        if (internal) {
            Account to = accounts.findByAccountNumber(req.toAccountNumber().trim())
                    .orElseThrow(() -> new NotFoundException("Destination account not found: " + req.toAccountNumber()));
            if (to.getId().equals(from.getId())) {
                throw new BusinessException("Cannot transfer to the same account.");
            }
            if (to.getStatus() != AccountStatus.ACTIVE) {
                throw new BusinessException("Destination account is " + to.getStatus() + ".");
            }
            to.setBalance(to.getBalance().add(amount));
            builder.toAccount(to);
            write(from, TxnType.TRANSFER_OUT, amount,
                    describe(req.description(), "Transfer to " + to.getAccountNumber()), to.getAccountNumber(), reference);
            write(to, TxnType.TRANSFER_IN, amount,
                    describe(req.description(), "Transfer from " + from.getAccountNumber()), from.getAccountNumber(), reference);
        } else {
            Beneficiary b = beneficiaries.findById(req.beneficiaryId())
                    .orElseThrow(() -> new NotFoundException("Beneficiary not found: id=" + req.beneficiaryId()));
            Access.requireOwnsCustomer(actor, b.getCustomer().getId());
            builder.beneficiary(b);
            write(from, TxnType.TRANSFER_OUT, amount,
                    describe(req.description(), "Payment to " + b.getName()), b.getAccountNumber(), reference);
        }

        return Mapper.toTransfer(transfers.save(builder.build()));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransferResponse> list(AppUserPrincipal actor, TransferStatus status, Pageable pageable) {
        Long customerId = actor.isAdmin() ? null : Access.requireCustomer(actor);
        return PageResponse.of(transfers.search(customerId, status, pageable).map(Mapper::toTransfer));
    }

    @Transactional(readOnly = true)
    public TransferResponse get(Long id, AppUserPrincipal actor) {
        Transfer t = find(id);
        Access.requireOwnsAccount(actor, t.getFromAccount());
        return Mapper.toTransfer(t);
    }

    /** ADMIN only: reverse a completed transfer, restoring balances and writing compensating rows. */
    public TransferResponse reverse(Long id) {
        Transfer t = find(id);
        if (t.getStatus() != TransferStatus.COMPLETED) {
            throw new BusinessException("Only COMPLETED transfers can be reversed (current=" + t.getStatus() + ").");
        }
        BigDecimal amount = t.getAmount();
        String reference = t.getReference() + "-REV";

        Account from = t.getFromAccount();
        from.setBalance(from.getBalance().add(amount));
        write(from, TxnType.TRANSFER_IN, amount, "Reversal of " + t.getReference(), null, reference);

        if (t.getToAccount() != null) {
            Account to = t.getToAccount();
            to.setBalance(to.getBalance().subtract(amount));
            write(to, TxnType.TRANSFER_OUT, amount, "Reversal of " + t.getReference(), null, reference);
        }
        t.setStatus(TransferStatus.REVERSED);
        return Mapper.toTransfer(t);
    }

    // ---- helpers ----
    private Transfer find(Long id) {
        return transfers.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer not found: id=" + id));
    }

    private String uniqueReference() {
        String ref;
        do {
            ref = Ids.transferReference();
        } while (transfers.findByReference(ref).isPresent());
        return ref;
    }

    private static String describe(String provided, String fallback) {
        return provided == null || provided.isBlank() ? fallback : provided;
    }

    private void write(Account a, TxnType type, BigDecimal amount,
                       String description, String counterparty, String reference) {
        Transaction t = Transaction.builder()
                .account(a).type(type).amount(amount).balanceAfter(a.getBalance())
                .description(description).counterparty(counterparty).reference(reference)
                .createdAt(Instant.now())
                .build();
        transactions.save(t);
    }
}
