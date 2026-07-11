package com.bank.qalab.error;

/** Maps to HTTP 422 (a valid request that breaks a business rule, e.g. insufficient funds). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
