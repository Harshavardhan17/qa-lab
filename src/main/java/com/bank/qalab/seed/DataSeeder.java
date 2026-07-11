package com.bank.qalab.seed;

import com.bank.qalab.domain.*;
import com.bank.qalab.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Populates the database on first start (only when it is empty). Data is generated with a fixed
 * random seed, so every fresh environment gets the SAME data — handy for repeatable QA scripts.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String CUSTOMER_PASSWORD = "Password123!";
    private static final String ADMIN_PASSWORD = "admin123";

    private final BranchRepository branches;
    private final UserRepository users;
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final TransferRepository transfers;
    private final BeneficiaryRepository beneficiaries;
    private final CardRepository cards;
    private final LoanRepository loans;
    private final PasswordEncoder encoder;

    @Value("${app.seed.enabled:true}")
    private boolean enabled;

    private final Random rng = new Random(42);
    private long accountSeq = 1_000_000_001L;
    private int txnCount = 0;

    private static final String[] DEPOSIT_DESCS = {
            "Salary credit", "Refund", "Interest credit", "Cash deposit", "Cheque clearing", "Cashback"
    };
    private static final String[] WITHDRAWAL_DESCS = {
            "ATM withdrawal", "Grocery store", "Online purchase", "Utility bill", "Restaurant", "Fuel", "Subscription"
    };

    public DataSeeder(BranchRepository branches, UserRepository users, CustomerRepository customers,
                      AccountRepository accounts, TransactionRepository transactions, TransferRepository transfers,
                      BeneficiaryRepository beneficiaries, CardRepository cards, LoanRepository loans,
                      PasswordEncoder encoder) {
        this.branches = branches;
        this.users = users;
        this.customers = customers;
        this.accounts = accounts;
        this.transactions = transactions;
        this.transfers = transfers;
        this.beneficiaries = beneficiaries;
        this.cards = cards;
        this.loans = loans;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            log.info("[seed] app.seed.enabled=false -> skipping data seeding");
            return;
        }
        if (users.count() > 0) {
            log.info("[seed] database already populated -> skipping ({} users present)", users.count());
            return;
        }

        log.info("[seed] populating demo banking data ...");
        List<Branch> branchList = seedBranches();
        seedAdmin();

        List<Account> allAccounts = new ArrayList<>();

        Person[] people = {
                new Person("Alice", "Anderson", "alice", "New York", "USA"),
                new Person("Bob", "Brown", "bob", "San Francisco", "USA"),
                new Person("Carol", "Clark", "carol", "Chicago", "USA"),
                new Person("David", "Davis", "dave", "Austin", "USA"),
                new Person("Eve", "Evans", "eve", "Seattle", "USA"),
                new Person("Frank", "Foster", "frank", "New York", "USA"),
                new Person("Grace", "Green", "grace", "Chicago", "USA"),
                new Person("Heidi", "Hughes", "heidi", "Austin", "USA"),
        };

        List<Customer> customerList = new ArrayList<>();
        for (int i = 0; i < people.length; i++) {
            Person p = people[i];
            Branch branch = branchList.get(i % branchList.size());
            Customer customer = createCustomer(p);
            createLogin(p.username, customer);
            customerList.add(customer);

            // Everyone gets a SAVINGS account; even indexes also get a CHECKING account.
            Account savings = createAccount(customer, AccountType.SAVINGS, branch);
            seedHistory(savings, money(2000 + rng.nextInt(6000)));
            allAccounts.add(savings);

            if (i % 2 == 0) {
                Account checking = createAccount(customer, AccountType.CHECKING, branch);
                seedHistory(checking, money(500 + rng.nextInt(3000)));
                allAccounts.add(checking);
            }

            // A debit card on the first account for most customers.
            if (i % 3 != 0) {
                issueCard(savings, p.firstName + " " + p.lastName, CardType.DEBIT, money(1500));
            }
        }

        // Credit cards for a couple of customers' first account.
        issueCard(allAccounts.get(0), "Alice Anderson", CardType.CREDIT, money(5000));
        issueCard(allAccounts.get(3), "David Davis", CardType.CREDIT, money(8000));

        // Beneficiaries.
        addBeneficiary(customerList.get(0), "Bob Brown", "landlord", "Metro Bank", "AC1000000003", "BQL0002");
        addBeneficiary(customerList.get(0), "Electric Co", "power bill", "Utility Bank", "EXT99881122", "UTIL0001");
        addBeneficiary(customerList.get(1), "Carol Clark", "friend", "Metro Bank", "AC1000000005", "BQL0003");
        addBeneficiary(customerList.get(3), "Landlord LLC", "rent", "Prime Bank", "EXT55667788", "PRIM0007");

        // A handful of historical transfers (internal + external) that keep balances consistent.
        internalTransfer(allAccounts.get(0), allAccounts.get(2), money(150), 12, "Dinner split");
        internalTransfer(allAccounts.get(1), allAccounts.get(4), money(320), 9, "Shared rent");
        internalTransfer(allAccounts.get(2), allAccounts.get(0), money(75), 5, "Movie tickets");
        internalTransfer(allAccounts.get(5), allAccounts.get(6), money(500), 3, "Gift");
        externalTransfer(allAccounts.get(0), beneficiaries.findByCustomerIdOrderByNameAsc(customerList.get(0).getId()).get(0), money(200), 7, "Monthly payment");

        // Loans across various statuses.
        createLoan(customerList.get(0), LoanType.PERSONAL, money(10000), new BigDecimal("9.500"), 24, LoanStatus.ACTIVE);
        createLoan(customerList.get(1), LoanType.AUTO, money(25000), new BigDecimal("7.250"), 60, LoanStatus.ACTIVE);
        createLoan(customerList.get(2), LoanType.HOME, money(250000), new BigDecimal("6.750"), 240, LoanStatus.APPLIED);
        createLoan(customerList.get(3), LoanType.EDUCATION, money(15000), new BigDecimal("5.000"), 36, LoanStatus.CLOSED);
        createLoan(customerList.get(4), LoanType.PERSONAL, money(5000), new BigDecimal("11.000"), 12, LoanStatus.REJECTED);

        log.info("[seed] done: {} branches, {} users, {} customers, {} accounts, {} transactions, {} transfers, {} beneficiaries, {} cards, {} loans",
                branches.count(), users.count(), customers.count(), accounts.count(), transactions.count(),
                transfers.count(), beneficiaries.count(), cards.count(), loans.count());
        log.info("[seed] logins -> admin/{}  |  customers: alice,bob,carol,dave,eve,frank,grace,heidi  (password: {})",
                ADMIN_PASSWORD, CUSTOMER_PASSWORD);
    }

    // ---------------- builders ----------------

    private List<Branch> seedBranches() {
        List<Branch> list = List.of(
                Branch.builder().name("Downtown Manhattan").ifsc("BQL0001").city("New York").address("100 Wall St").build(),
                Branch.builder().name("Market Street").ifsc("BQL0002").city("San Francisco").address("55 Market St").build(),
                Branch.builder().name("Loop Central").ifsc("BQL0003").city("Chicago").address("12 State St").build(),
                Branch.builder().name("Congress Ave").ifsc("BQL0004").city("Austin").address("200 Congress Ave").build(),
                Branch.builder().name("Pike Place").ifsc("BQL0005").city("Seattle").address("9 Pike St").build()
        );
        return branches.saveAll(list);
    }

    private void seedAdmin() {
        AppUser admin = AppUser.builder()
                .username("admin")
                .email("admin@bankqa.local")
                .password(encoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .enabled(true)
                .customer(null)
                .build();
        users.save(admin);
    }

    private Customer createCustomer(Person p) {
        Customer c = Customer.builder()
                .firstName(p.firstName)
                .lastName(p.lastName)
                .email(p.username + "@bankqa.local")
                .phone("+1-555-01" + (10 + rng.nextInt(89)))
                .dateOfBirth(LocalDate.of(1980 + rng.nextInt(20), 1 + rng.nextInt(12), 1 + rng.nextInt(27)))
                .addressLine((100 + rng.nextInt(900)) + " Main St")
                .city(p.city)
                .country(p.country)
                .status(CustomerStatus.ACTIVE)
                .build();
        return customers.save(c);
    }

    private void createLogin(String username, Customer customer) {
        AppUser user = AppUser.builder()
                .username(username)
                .email(customer.getEmail())
                .password(encoder.encode(CUSTOMER_PASSWORD))
                .role(Role.CUSTOMER)
                .enabled(true)
                .customer(customer)
                .build();
        users.save(user);
    }

    private Account createAccount(Customer customer, AccountType type, Branch branch) {
        Account a = Account.builder()
                .accountNumber("AC" + (accountSeq++))
                .customer(customer)
                .type(type)
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .branch(branch)
                .openedAt(Instant.now().minus(200, ChronoUnit.DAYS))
                .build();
        return accounts.save(a);
    }

    /** Creates an opening deposit plus a spread of deposits/withdrawals; sets the final balance. */
    private void seedHistory(Account account, BigDecimal opening) {
        BigDecimal balance = BigDecimal.ZERO;
        Instant start = Instant.now().minus(120, ChronoUnit.DAYS);

        balance = balance.add(opening);
        writeTxn(account, TxnType.DEPOSIT, opening, balance, "Opening deposit", null, null, start);

        int n = 8 + rng.nextInt(8);
        for (int i = 1; i <= n; i++) {
            Instant when = start.plus((long) (i * (110.0 / n)), ChronoUnit.DAYS);
            if (rng.nextBoolean()) {
                BigDecimal amt = money(50 + rng.nextInt(2000));
                balance = balance.add(amt);
                writeTxn(account, TxnType.DEPOSIT, amt, balance, pick(DEPOSIT_DESCS), null, null, when);
            } else {
                BigDecimal amt = money(20 + rng.nextInt(800));
                if (balance.compareTo(amt) >= 0) {
                    balance = balance.subtract(amt);
                    writeTxn(account, TxnType.WITHDRAWAL, amt, balance, pick(WITHDRAWAL_DESCS), null, null, when);
                }
            }
        }
        account.setBalance(balance);
        accounts.save(account);
    }

    private void internalTransfer(Account from, Account to, BigDecimal amount, int daysAgo, String desc) {
        if (from.getBalance().compareTo(amount) < 0) return;
        Instant when = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        String ref = "TRF-SEED-" + (1000 + rng.nextInt(9000));

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        accounts.save(from);
        accounts.save(to);

        writeTxn(from, TxnType.TRANSFER_OUT, amount, from.getBalance(), desc, to.getAccountNumber(), ref, when);
        writeTxn(to, TxnType.TRANSFER_IN, amount, to.getBalance(), desc, from.getAccountNumber(), ref, when);

        transfers.save(Transfer.builder()
                .reference(ref).fromAccount(from).toAccount(to).amount(amount).currency("USD")
                .description(desc).status(TransferStatus.COMPLETED).createdAt(when).completedAt(when).build());
    }

    private void externalTransfer(Account from, Beneficiary beneficiary, BigDecimal amount, int daysAgo, String desc) {
        if (from.getBalance().compareTo(amount) < 0) return;
        Instant when = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        String ref = "TRF-SEED-" + (1000 + rng.nextInt(9000));

        from.setBalance(from.getBalance().subtract(amount));
        accounts.save(from);
        writeTxn(from, TxnType.TRANSFER_OUT, amount, from.getBalance(), desc, beneficiary.getAccountNumber(), ref, when);

        transfers.save(Transfer.builder()
                .reference(ref).fromAccount(from).beneficiary(beneficiary).amount(amount).currency("USD")
                .description(desc).status(TransferStatus.COMPLETED).createdAt(when).completedAt(when).build());
    }

    private void addBeneficiary(Customer customer, String name, String nickname, String bank, String acct, String ifsc) {
        beneficiaries.save(Beneficiary.builder()
                .customer(customer).name(name).nickname(nickname).bankName(bank)
                .accountNumber(acct).ifsc(ifsc).build());
    }

    private void issueCard(Account account, String holder, CardType type, BigDecimal limit) {
        int bin = type == CardType.CREDIT ? 5100 + rng.nextInt(200) : 4000 + rng.nextInt(200);
        String masked = String.format("%d-XXXX-XXXX-%04d", bin, rng.nextInt(10000));
        cards.save(Card.builder()
                .account(account).cardNumber(masked).cardHolder(holder).type(type)
                .status(CardStatus.ACTIVE).expiryDate(LocalDate.now().plusYears(4).withDayOfMonth(1))
                .dailyLimit(limit).build());
    }

    private void createLoan(Customer customer, LoanType type, BigDecimal principal, BigDecimal rate,
                            int months, LoanStatus status) {
        BigDecimal emi = com.bank.qalab.service.LoanService.computeEmi(principal, rate, months);
        BigDecimal outstanding = switch (status) {
            case CLOSED, REJECTED -> BigDecimal.ZERO.setScale(2);
            case ACTIVE -> principal.multiply(new BigDecimal("0.80")).setScale(2, java.math.RoundingMode.HALF_UP);
            default -> principal;
        };
        LocalDate start = (status == LoanStatus.ACTIVE || status == LoanStatus.CLOSED)
                ? LocalDate.now().minusMonths(6) : null;
        loans.save(Loan.builder()
                .customer(customer).type(type).principal(principal).interestRate(rate).termMonths(months)
                .emiAmount(emi).outstanding(outstanding).status(status).startDate(start).build());
    }

    // ---------------- helpers ----------------

    private void writeTxn(Account account, TxnType type, BigDecimal amount, BigDecimal balanceAfter,
                          String description, String counterparty, String reference, Instant when) {
        transactions.save(Transaction.builder()
                .account(account).type(type).amount(amount).balanceAfter(balanceAfter)
                .description(description).counterparty(counterparty).reference(reference).createdAt(when)
                .build());
        txnCount++;
    }

    private static BigDecimal money(long whole) {
        return BigDecimal.valueOf(whole).setScale(2);
    }

    private String pick(String[] arr) {
        return arr[rng.nextInt(arr.length)];
    }

    private record Person(String firstName, String lastName, String username, String city, String country) {}
}
