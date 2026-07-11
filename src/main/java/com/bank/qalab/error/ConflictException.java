package com.bank.qalab.error;

/** Maps to HTTP 409 (e.g. duplicate username/email/account number). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
