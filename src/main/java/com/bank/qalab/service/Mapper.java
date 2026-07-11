package com.bank.qalab.service;

import com.bank.qalab.domain.*;
import com.bank.qalab.dto.AccountDtos.AccountResponse;
import com.bank.qalab.dto.BeneficiaryDtos.BeneficiaryResponse;
import com.bank.qalab.dto.BranchDtos.BranchResponse;
import com.bank.qalab.dto.CardDtos.CardResponse;
import com.bank.qalab.dto.CustomerDtos.CustomerResponse;
import com.bank.qalab.dto.LoanDtos.LoanResponse;
import com.bank.qalab.dto.TransactionDtos.TransactionResponse;
import com.bank.qalab.dto.TransferDtos.TransferResponse;

/**
 * Maps entities to response DTOs. Call these from inside a transactional service method so that
 * lazy associations (customer, account, branch) can be read safely.
 */
public final class Mapper {

    private Mapper() {}

    public static CustomerResponse toCustomer(Customer c) {
        return new CustomerResponse(c.getId(), c.getFirstName(), c.getLastName(), c.getEmail(),
                c.getPhone(), c.getDateOfBirth(), c.getAddressLine(), c.getCity(), c.getCountry(),
                c.getStatus().name(), c.getCreatedAt());
    }

    public static AccountResponse toAccount(Account a) {
        Customer c = a.getCustomer();
        String customerName = c != null ? c.getFirstName() + " " + c.getLastName() : null;
        Long customerId = c != null ? c.getId() : null;
        String ifsc = a.getBranch() != null ? a.getBranch().getIfsc() : null;
        return new AccountResponse(a.getId(), a.getAccountNumber(), customerId, customerName,
                a.getType().name(), a.getStatus().name(), a.getBalance(), a.getCurrency(), ifsc, a.getOpenedAt());
    }

    public static TransactionResponse toTransaction(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAccount().getId(), t.getType().name(),
                t.getAmount(), t.getBalanceAfter(), t.getDescription(), t.getCounterparty(),
                t.getReference(), t.getCreatedAt());
    }

    public static TransferResponse toTransfer(Transfer t) {
        Account from = t.getFromAccount();
        Account to = t.getToAccount();
        Beneficiary b = t.getBeneficiary();
        return new TransferResponse(
                t.getId(), t.getReference(),
                from != null ? from.getId() : null,
                from != null ? from.getAccountNumber() : null,
                to != null ? to.getId() : null,
                to != null ? to.getAccountNumber() : null,
                b != null ? b.getId() : null,
                b != null ? b.getName() : null,
                t.getAmount(), t.getCurrency(), t.getDescription(), t.getStatus().name(),
                t.getCreatedAt(), t.getCompletedAt());
    }

    public static BeneficiaryResponse toBeneficiary(Beneficiary b) {
        return new BeneficiaryResponse(b.getId(), b.getCustomer().getId(), b.getName(), b.getNickname(),
                b.getBankName(), b.getAccountNumber(), b.getIfsc(), b.getCreatedAt());
    }

    public static CardResponse toCard(Card c) {
        Account a = c.getAccount();
        return new CardResponse(c.getId(), a.getId(), a.getAccountNumber(), c.getCardNumber(),
                c.getCardHolder(), c.getType().name(), c.getStatus().name(), c.getExpiryDate(),
                c.getDailyLimit(), c.getCreatedAt());
    }

    public static LoanResponse toLoan(Loan l) {
        return new LoanResponse(l.getId(), l.getCustomer().getId(), l.getType().name(), l.getPrincipal(),
                l.getInterestRate(), l.getTermMonths(), l.getEmiAmount(), l.getOutstanding(),
                l.getStatus().name(), l.getStartDate(), l.getCreatedAt());
    }

    public static BranchResponse toBranch(Branch b) {
        return new BranchResponse(b.getId(), b.getName(), b.getIfsc(), b.getCity(), b.getAddress());
    }
}
