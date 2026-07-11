package com.bank.qalab.service;

import com.bank.qalab.domain.Account;
import com.bank.qalab.domain.AccountStatus;
import com.bank.qalab.domain.AccountType;
import com.bank.qalab.domain.Branch;
import com.bank.qalab.domain.Customer;
import com.bank.qalab.domain.Transaction;
import com.bank.qalab.domain.TxnType;
import com.bank.qalab.dto.AccountDtos.AccountResponse;
import com.bank.qalab.dto.AccountDtos.MoneyRequest;
import com.bank.qalab.dto.AccountDtos.OpenAccountRequest;
import com.bank.qalab.dto.PageResponse;
import com.bank.qalab.dto.TransactionDtos.TransactionResponse;
import com.bank.qalab.error.BusinessException;
import com.bank.qalab.error.ConflictException;
import com.bank.qalab.error.NotFoundException;
import com.bank.qalab.repo.AccountRepository;
import com.bank.qalab.repo.BranchRepository;
import com.bank.qalab.repo.CustomerRepository;
import com.bank.qalab.repo.TransactionRepository;
import com.bank.qalab.security.AppUserPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final BranchRepository branches;
    private final TransactionRepository transactions;

    public AccountService(AccountRepository accounts, CustomerRepository customers,
                          BranchRepository branches, TransactionRepository transactions) {
        this.accounts = accounts;
        this.customers = customers;
        this.branches = branches;
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> list(AppUserPrincipal actor, AccountStatus status,
                                              AccountType type, Pageable pageable) {
        Long customerId = actor.isAdmin() ? null : Access.requireCustomer(actor);
        return PageResponse.of(accounts.search(customerId, status, type, pageable).map(Mapper::toAccount));
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long id, AppUserPrincipal actor) {
        Account a = find(id);
        Access.requireOwnsAccount(actor, a);
        return Mapper.toAccount(a);
    }

    public AccountResponse open(OpenAccountRequest req, AppUserPrincipal actor) {
        Long targetCustomerId;
        if (actor.isAdmin()) {
            if (req.customerId() == null) {
                throw new BusinessException("ADMIN must supply 'customerId' when opening an account.");
            }
            targetCustomerId = req.customerId();
        } else {
            targetCustomerId = Access.requireCustomer(actor);
        }
        Customer customer = customers.findById(targetCustomerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: id=" + targetCustomerId));

        Branch branch = null;
        if (req.branchIfsc() != null && !req.branchIfsc().isBlank()) {
            branch = branches.findByIfsc(req.branchIfsc())
                    .orElseThrow(() -> new NotFoundException("Branch not found: " + req.branchIfsc()));
        }

        String number;
        do {
            number = Ids.accountNumber();
        } while (accounts.existsByAccountNumber(number));

        BigDecimal initial = req.initialDeposit() == null
                ? BigDecimal.ZERO
                : req.initialDeposit().setScale(2, RoundingMode.HALF_UP);

        Account account = Account.builder()
                .accountNumber(number)
                .customer(customer)
                .type(req.type())
                .status(AccountStatus.ACTIVE)
                .balance(initial)
                .currency(req.currency() == null || req.currency().isBlank() ? "USD" : req.currency().toUpperCase())
                .branch(branch)
                .build();
        accounts.save(account);

        if (initial.signum() > 0) {
            record(account, TxnType.DEPOSIT, initial, "Opening deposit", null, null);
        }
        return Mapper.toAccount(account);
    }

    public TransactionResponse deposit(Long id, MoneyRequest req, AppUserPrincipal actor) {
        Account a = find(id);
        Access.requireOwnsAccount(actor, a);
        ensureActive(a);
        BigDecimal amount = normalize(req.amount());
        a.setBalance(a.getBalance().add(amount));
        Transaction t = record(a, TxnType.DEPOSIT, amount, describe(req.description(), "Deposit"), null, null);
        return Mapper.toTransaction(t);
    }

    public TransactionResponse withdraw(Long id, MoneyRequest req, AppUserPrincipal actor) {
        Account a = find(id);
        Access.requireOwnsAccount(actor, a);
        ensureActive(a);
        BigDecimal amount = normalize(req.amount());
        if (a.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds. Balance=" + a.getBalance() + ", requested=" + amount);
        }
        a.setBalance(a.getBalance().subtract(amount));
        Transaction t = record(a, TxnType.WITHDRAWAL, amount, describe(req.description(), "Withdrawal"), null, null);
        return Mapper.toTransaction(t);
    }

    public AccountResponse updateStatus(Long id, AccountStatus status, AppUserPrincipal actor) {
        Account a = find(id);
        Access.requireOwnsAccount(actor, a);
        a.setStatus(status);
        return Mapper.toAccount(accounts.save(a));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> transactions(Long id, Instant from, Instant to,
                                                          Pageable pageable, AppUserPrincipal actor) {
        Account a = find(id);
        Access.requireOwnsAccount(actor, a);
        Instant fromTs = (from == null) ? Instant.EPOCH : from;
        Instant toTs = (to == null) ? Instant.parse("2999-12-31T23:59:59Z") : to;
        return PageResponse.of(transactions.findForAccount(id, fromTs, toTs, pageable).map(Mapper::toTransaction));
    }

    /** Soft-close (DELETE): keeps the row for auditing but sets status CLOSED. */
    public void close(Long id) {
        Account a = find(id);
        if (a.getBalance().signum() != 0) {
            throw new ConflictException("Account balance must be zero to close (current=" + a.getBalance() + ").");
        }
        a.setStatus(AccountStatus.CLOSED);
        accounts.save(a);
    }

    // ---- helpers ----
    private Account find(Long id) {
        return accounts.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found: id=" + id));
    }

    private void ensureActive(Account a) {
        if (a.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is " + a.getStatus() + "; only ACTIVE accounts allow this operation.");
        }
    }

    private static BigDecimal normalize(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static String describe(String provided, String fallback) {
        return provided == null || provided.isBlank() ? fallback : provided;
    }

    private Transaction record(Account a, TxnType type, BigDecimal amount,
                               String description, String counterparty, String reference) {
        Transaction t = Transaction.builder()
                .account(a).type(type).amount(amount).balanceAfter(a.getBalance())
                .description(description).counterparty(counterparty).reference(reference)
                .build();
        return transactions.save(t);
    }
}
