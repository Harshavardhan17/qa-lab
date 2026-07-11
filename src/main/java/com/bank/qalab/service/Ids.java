package com.bank.qalab.service;

import java.security.SecureRandom;

/** Generates human-readable, unique-ish identifiers for accounts, transfers and cards. */
public final class Ids {

    private static final SecureRandom RNG = new SecureRandom();

    private Ids() {}

    public static String accountNumber() {
        StringBuilder sb = new StringBuilder("AC");
        for (int i = 0; i < 10; i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }

    public static String transferReference() {
        return "TRF-" + System.currentTimeMillis() + "-" + (100 + RNG.nextInt(900));
    }

    public static String maskedCardNumber() {
        int bin = 4000 + RNG.nextInt(1000);
        int last4 = RNG.nextInt(10000);
        return String.format("%d-XXXX-XXXX-%04d", bin, last4);
    }
}
