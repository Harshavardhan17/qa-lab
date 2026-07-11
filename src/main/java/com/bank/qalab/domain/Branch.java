package com.bank.qalab.domain;

import jakarta.persistence.*;
import lombok.*;

/** A physical bank branch (reference data). */
@Entity
@Table(name = "branch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Branch/routing code (IFSC-style). */
    @Column(nullable = false, unique = true, length = 20)
    private String ifsc;

    @Column(nullable = false)
    private String city;

    private String address;
}
