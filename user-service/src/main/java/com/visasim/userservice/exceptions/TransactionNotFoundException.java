package com.visasim.userservice.exceptions;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(UUID id) {
        super("Transaction not found with id: " + id);
    }
}