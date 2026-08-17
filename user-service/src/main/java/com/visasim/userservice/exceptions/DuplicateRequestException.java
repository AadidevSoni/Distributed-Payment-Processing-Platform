package com.visasim.userservice.exceptions;

public class DuplicateRequestException extends RuntimeException {
    public DuplicateRequestException(String idempotencyKey) {
        super("Duplicate request detected for idempotency key: " + idempotencyKey
                + ". This request has already been processed.");
    }
}