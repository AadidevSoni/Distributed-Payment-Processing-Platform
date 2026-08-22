package com.visasim.fraudservice.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fraud_checks")
public class FraudCheck {

    @Id
    private UUID id;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "from_wallet_id", nullable = false)
    private UUID fromWalletId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private FraudDecision decision;

    @Column(name = "reasons", columnDefinition = "TEXT")
    private String reasons;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FraudCheck() {
    }

    public FraudCheck(UUID fromWalletId, int riskScore, FraudDecision decision, String reasons) {
        this.id = UUID.randomUUID();
        this.fromWalletId = fromWalletId;
        this.riskScore = riskScore;
        this.decision = decision;
        this.reasons = reasons;
        this.createdAt = Instant.now();
    }

    public void linkToTransaction(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getId() { return id; }
    public UUID getTransactionId() { return transactionId; }
    public UUID getFromWalletId() { return fromWalletId; }
    public int getRiskScore() { return riskScore; }
    public FraudDecision getDecision() { return decision; }
    public String getReasons() { return reasons; }
    public Instant getCreatedAt() { return createdAt; }
}