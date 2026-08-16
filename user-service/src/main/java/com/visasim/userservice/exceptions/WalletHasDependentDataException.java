package com.visasim.userservice.exceptions;

import java.util.UUID;

public class WalletHasDependentDataException extends RuntimeException {
    public WalletHasDependentDataException(UUID walletId) {
        super("Cannot delete wallet " + walletId + ": wallet has existing transaction history.");
    }
}