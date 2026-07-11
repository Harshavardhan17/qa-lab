package com.bank.qalab.error;

/** Maps to HTTP 403 (authenticated but not allowed to touch this particular resource). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
