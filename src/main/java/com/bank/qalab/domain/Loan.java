package com.bank.qalab.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** A loan applied for / held by a customer. */
@Entity
@Table(name = "loan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principal;

    /** Annual interest rate in percent, e.g. 8.500. */
    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(precision = 19, scale = 2)
    private BigDecimal emiAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal outstanding;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    private LocalDate startDate;

    @Column(updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = LoanStatus.APPLIED;
    }
}
