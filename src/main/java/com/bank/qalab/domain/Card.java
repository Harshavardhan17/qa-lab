package com.bank.qalab.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** A debit/credit card linked to an account. */
@Entity
@Table(name = "card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    /** Masked PAN, e.g. 4111-XXXX-XXXX-4321. */
    @Column(nullable = false, length = 25)
    private String cardNumber;

    @Column(nullable = false)
    private String cardHolder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    private LocalDate expiryDate;

    @Column(precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @Column(updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = CardStatus.ACTIVE;
    }
}
