package com.bank.qalab.dto;

import java.time.Instant;
import java.util.List;

/** Consistent error body returned for every 4xx/5xx from the API. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}
}
