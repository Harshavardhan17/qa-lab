package com.bank.qalab.service;

import com.bank.qalab.domain.Account;
import com.bank.qalab.domain.AccountStatus;
import com.bank.qalab.domain.Customer;
import com.bank.qalab.domain.Loan;
import com.bank.qalab.domain.LoanStatus;
import com.bank.qalab.domain.Transaction;
import com.bank.qalab.domain.TxnType;
import com.bank.qalab.dto.LoanDtos.LoanApplicationRequest;
import com.bank.qalab.dto.LoanDtos.LoanResponse;
import com.bank.qalab.dto.LoanDtos.RepayRequest;
import com.bank.qalab.dto.PageResponse;
import com.bank.qalab.error.BusinessException;
import com.bank.qalab.error.NotFoundException;
import com.bank.qalab.repo.AccountRepository;
import com.bank.qalab.repo.CustomerRepository;
import com.bank.qalab.repo.LoanRepository;
import com.bank.qalab.repo.TransactionRepository;
import com.bank.qalab.security.AppUserPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional
public class LoanService {

    private final LoanRepository loans;
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public LoanService(LoanRepository loans, CustomerRepository customers,
                       AccountRepository accounts, TransactionRepository transactions) {
        this.loans = loans;
        this.customers = customers;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public PageResponse<LoanResponse> list(AppUserPrincipal actor, LoanStatus status, Pageable pageable) {
        Long customerId = actor.isAdmin() ? null : Access.requireCustomer(actor);
        return PageResponse.of(loans.search(customerId, status, pageable).map(Mapper::toLoan));
    }

    @Transactional(readOnly = true)
    public LoanResponse get(Long id, AppUserPrincipal actor) {
        Loan l = find(id);
        if (!actor.isAdmin()) {
            Access.requireOwnsCustomer(actor, l.getCustomer().getId());
        }
        return Mapper.toLoan(l);
    }

    public LoanResponse apply(LoanApplicationRequest req, AppUserPrincipal actor) {
        Long customerId = Access.requireCustomer(actor);
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: id=" + customerId));
        BigDecimal principal = req.principal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal emi = computeEmi(principal, req.interestRate(), req.termMonths());
        Loan loan = Loan.builder()
                .customer(customer)
                .type(req.type())
                .principal(principal)
                .interestRate(req.interestRate())
                .termMonths(req.termMonths())
                .emiAmount(emi)
                .outstanding(principal)
                .status(LoanStatus.APPLIED)
                .build();
        return Mapper.toLoan(loans.save(loan));
    }

    public LoanResponse approve(Long id) {
        Loan l = find(id);
        if (l.getStatus() != LoanStatus.APPLIED) {
            throw new BusinessException("Only APPLIED loans can be approved (current=" + l.getStatus() + ").");
        }
        l.setStatus(LoanStatus.ACTIVE);
        l.setStartDate(LocalDate.now());
        l.setOutstanding(l.getPrincipal());
        return Mapper.toLoan(loans.save(l));
    }

    public LoanResponse reject(Long id) {
        Loan l = find(id);
        if (l.getStatus() != LoanStatus.APPLIED) {
            throw new BusinessException("Only APPLIED loans can be rejected (current=" + l.getStatus() + ").");
        }
        l.setStatus(LoanStatus.REJECTED);
        return Mapper.toLoan(loans.save(l));
    }

    public LoanResponse repay(Long id, RepayRequest req, AppUserPrincipal actor) {
        Loan l = find(id);
        if (!actor.isAdmin()) {
            Access.requireOwnsCustomer(actor, l.getCustomer().getId());
        }
        if (l.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("Only ACTIVE loans can be repaid (current=" + l.getStatus() + ").");
        }
        BigDecimal amount = req.amount().setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(l.getOutstanding()) > 0) {
            throw new BusinessException("Amount exceeds outstanding balance (" + l.getOutstanding() + ").");
        }
        if (req.fromAccountId() != null) {
            Account a = accounts.findById(req.fromAccountId())
                    .orElseThrow(() -> new NotFoundException("Account not found: id=" + req.fromAccountId()));
            Access.requireOwnsAccount(actor, a);
            if (a.getStatus() != AccountStatus.ACTIVE) {
                throw new BusinessException("Account is " + a.getStatus() + ".");
            }
            if (a.getBalance().compareTo(amount) < 0) {
                throw new BusinessException("Insufficient funds in account for repayment.");
            }
            a.setBalance(a.getBalance().subtract(amount));
            Transaction t = Transaction.builder()
                    .account(a).type(TxnType.LOAN_REPAYMENT).amount(amount).balanceAfter(a.getBalance())
                    .description("Loan repayment #" + l.getId()).reference("LOAN-" + l.getId())
                    .build();
            transactions.save(t);
        }
        l.setOutstanding(l.getOutstanding().subtract(amount));
        if (l.getOutstanding().signum() == 0) {
            l.setStatus(LoanStatus.CLOSED);
        }
        return Mapper.toLoan(loans.save(l));
    }

    private Loan find(Long id) {
        return loans.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan not found: id=" + id));
    }

    /** Standard EMI (amortising) formula, rounded to 2 dp. */
    public static BigDecimal computeEmi(BigDecimal principal, BigDecimal annualRatePct, int months) {
        double p = principal.doubleValue();
        double r = annualRatePct.doubleValue() / 12.0 / 100.0;
        double emi = (r == 0)
                ? p / months
                : p * r * Math.pow(1 + r, months) / (Math.pow(1 + r, months) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }
}
