package com.visasim.fraudservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionHistoryResponse(
        UUID id,
        UUID fromWalletId,
        UUID toWalletId,
        BigDecimal amount,
        String status,
        Instant createdAt,
        Instant completedAt
) {
}