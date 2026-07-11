package com.bank.qalab.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/** A single ledger movement on one account. */
@Entity
@Table(name = "account_transaction", indexes = {
        @Index(name = "idx_txn_account", columnList = "account_id"),
        @Index(name = "idx_txn_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TxnType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** Running balance of the account immediately after this transaction. */
    @Column(precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    private String description;

    /** Account number or name on the other side of the movement. */
    private String counterparty;

    /** Correlates transactions that belong to the same transfer. */
    private String reference;

    @Column(updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
