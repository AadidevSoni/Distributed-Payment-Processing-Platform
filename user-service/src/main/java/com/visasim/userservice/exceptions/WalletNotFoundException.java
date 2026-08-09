package com.visasim.userservice.exceptions;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(UUID id) {
        super("Wallet not found with id: " + id);
    }
}