package com.visasim.userservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.visasim.userservice.model.Wallet;

public record WalletResponse(
        UUID id,
        UUID userId,
        BigDecimal balance,
        String currency,
        Instant updatedAt
) {
    public static WalletResponse fromWallet(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getUpdatedAt()
        );
    }
}