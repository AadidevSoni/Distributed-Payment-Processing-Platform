package com.visasim.userservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCompletedEvent(
        UUID transactionId,
        UUID fromWalletId,
        UUID toWalletId,
        BigDecimal amount,
        String status,
        Instant occurredAt
) {}