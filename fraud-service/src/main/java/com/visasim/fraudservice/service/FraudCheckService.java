package com.visasim.fraudservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.visasim.fraudservice.client.UserServiceClient;
import com.visasim.fraudservice.dto.TransactionHistoryResponse;
import com.visasim.fraudservice.model.FraudCheck;
import com.visasim.fraudservice.model.FraudDecision;
import com.visasim.fraudservice.repository.FraudCheckRepository;

@Service
public class FraudCheckService {

    private static final int VELOCITY_WINDOW_MINUTES = 1;
    private static final int VELOCITY_MAX_TRANSFERS = 5;
    private static final int VELOCITY_RISK_POINTS = 40;

    private static final BigDecimal LARGE_AMOUNT_MULTIPLIER =
            new BigDecimal("5");

    private static final int LARGE_AMOUNT_RISK_POINTS = 30;

    private static final int SELF_TRANSFER_RISK_POINTS = 100;

    private static final int BLOCK_THRESHOLD = 70;
    private static final int FLAG_THRESHOLD = 30;

    private final StringRedisTemplate redisTemplate;
    private final UserServiceClient userServiceClient;
    private final FraudCheckRepository fraudCheckRepository;

    public FraudCheckService(
            StringRedisTemplate redisTemplate,
            UserServiceClient userServiceClient,
            FraudCheckRepository fraudCheckRepository) {

        this.redisTemplate = redisTemplate;
        this.userServiceClient = userServiceClient;
        this.fraudCheckRepository = fraudCheckRepository;
    }

    public FraudCheck evaluate(
            UUID fromWalletId,
            UUID toWalletId,
            BigDecimal amount) {

        int riskScore = 0;
        StringBuilder reasons = new StringBuilder();

        // Rule 1: self-transfer
        if (fromWalletId.equals(toWalletId)) {
            riskScore += SELF_TRANSFER_RISK_POINTS;
            reasons.append("Self-transfer detected. ");
        }

        // Rule 2: velocity check
        String velocityKey = "fraud-velocity:" + fromWalletId;

        Long recentCount =
                redisTemplate.opsForValue().increment(velocityKey);

        if (recentCount != null && recentCount == 1L) {
            redisTemplate.expire(
                    velocityKey,
                    Duration.ofMinutes(VELOCITY_WINDOW_MINUTES));
        }

        if (recentCount != null
                && recentCount > VELOCITY_MAX_TRANSFERS) {

            riskScore += VELOCITY_RISK_POINTS;

            reasons.append("Velocity exceeded: ")
                    .append(recentCount)
                    .append(" transfers in ")
                    .append(VELOCITY_WINDOW_MINUTES)
                    .append(" min. ");
        }

        // Rule 3: large-amount check
        List<TransactionHistoryResponse> history =
                userServiceClient.getTransactionHistory(fromWalletId);

        if (!history.isEmpty()) {

            BigDecimal average = history.stream()
                    .map(TransactionHistoryResponse::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(
                            new BigDecimal(history.size()),
                            4,
                            RoundingMode.HALF_UP);

            BigDecimal threshold =
                    average.multiply(LARGE_AMOUNT_MULTIPLIER);

            if (amount.compareTo(threshold) > 0) {

                riskScore += LARGE_AMOUNT_RISK_POINTS;

                reasons.append("Amount ")
                        .append(amount)
                        .append(" exceeds 5x historical average (")
                        .append(average)
                        .append("). ");
            }
        }

        FraudDecision decision;

        if (riskScore >= BLOCK_THRESHOLD) {
            decision = FraudDecision.BLOCK;
        } else if (riskScore >= FLAG_THRESHOLD) {
            decision = FraudDecision.FLAG;
        } else {
            decision = FraudDecision.ALLOW;
        }

        FraudCheck check = new FraudCheck(
                fromWalletId,
                riskScore,
                decision,
                reasons.length() > 0
                        ? reasons.toString()
                        : "No risk factors detected."
        );

        return fraudCheckRepository.save(check);
    }

    public void linkTransaction(UUID fraudCheckId, UUID transactionId) {
        FraudCheck check = fraudCheckRepository.findById(fraudCheckId)
                .orElseThrow(() -> new RuntimeException("FraudCheck not found: " + fraudCheckId));
        check.linkToTransaction(transactionId);
        fraudCheckRepository.save(check);
    }
}