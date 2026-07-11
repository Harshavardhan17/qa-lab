package com.bank.qalab.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** A saved payee that a customer can transfer money to. */
@Entity
@Table(name = "beneficiary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private String name;

    private String nickname;

    private String bankName;

    @Column(nullable = false, length = 34)
    private String accountNumber;

    private String ifsc;

    @Column(updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
