package com.visasim.fraudservice.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visasim.fraudservice.model.FraudCheck;
import com.visasim.fraudservice.service.FraudCheckService;

@RestController
@RequestMapping("/fraud-checks")
public class FraudCheckController {

    private final FraudCheckService fraudCheckService;

    public FraudCheckController(FraudCheckService fraudCheckService) {
        this.fraudCheckService = fraudCheckService;
    }

    public record EvaluateRequest(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {}

    public record EvaluateResponse(UUID id, int riskScore, String decision, String reasons) {}

    @PostMapping("/evaluate")
    public EvaluateResponse evaluate(@RequestBody EvaluateRequest request) {
        FraudCheck check = fraudCheckService.evaluate(
                request.fromWalletId(), request.toWalletId(), request.amount());
        return new EvaluateResponse(
                check.getId(), check.getRiskScore(), check.getDecision().name(), check.getReasons());
    }

    @PatchMapping("/{fraudCheckId}/link")
    public void linkTransaction(
            @PathVariable UUID fraudCheckId,
            @RequestBody UUID transactionId) {
        fraudCheckService.linkTransaction(fraudCheckId, transactionId);
    }
}