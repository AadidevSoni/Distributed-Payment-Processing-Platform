package com.visasim.userservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.visasim.userservice.model.Transaction;
import com.visasim.userservice.model.TransactionStatus;

public record TransactionResponse(
        UUID id,
        UUID fromWalletId,
        UUID toWalletId,
        BigDecimal amount,
        TransactionStatus status,
        Instant createdAt,
        Instant completedAt
) {
    public static TransactionResponse fromTransaction(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getFromWalletId(),
                tx.getToWalletId(),
                tx.getAmount(),
                tx.getStatus(),
                tx.getCreatedAt(),
                tx.getCompletedAt()
        );
    }
}