package com.visasim.userservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Column(name = "from_wallet_id", nullable = false)
    private UUID fromWalletId;

    @Column(name = "to_wallet_id", nullable = false)
    private UUID toWalletId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected Transaction() {
    }

    public Transaction(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
        this.status = TransactionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markFailed() {
        this.status = TransactionStatus.FAILED;
        this.completedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getFromWalletId() {
        return fromWalletId;
    }

    public UUID getToWalletId() {
        return toWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}