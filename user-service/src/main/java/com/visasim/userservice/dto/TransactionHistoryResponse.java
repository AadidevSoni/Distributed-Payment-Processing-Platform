package com.visasim.userservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.visasim.userservice.model.Transaction;

public record TransactionHistoryResponse(
        UUID id,
        UUID fromWalletId,
        UUID toWalletId,
        BigDecimal amount,
        String status,
        Instant createdAt,
        Instant completedAt
) {
    public static TransactionHistoryResponse fromTransaction(Transaction tx) {
        return new TransactionHistoryResponse(
                tx.getId(),
                tx.getFromWalletId(),
                tx.getToWalletId(),
                tx.getAmount(),
                tx.getStatus().name(),
                tx.getCreatedAt(),
                tx.getCompletedAt()
        );
    }
}