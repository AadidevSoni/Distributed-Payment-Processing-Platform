package com.visasim.userservice.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FraudServiceClient {

    private final RestClient restClient;

    public FraudServiceClient(RestClient.Builder restClientBuilder,
                            @Value("${fraud-service.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public record EvaluateRequest(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {}
    public record EvaluateResponse(UUID id, int riskScore, String decision, String reasons) {}

    public EvaluateResponse evaluate(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        return restClient.post()
                .uri("/fraud-checks/evaluate")
                .body(new EvaluateRequest(fromWalletId, toWalletId, amount))
                .retrieve()
                .body(EvaluateResponse.class);
    }

    public void linkTransaction(UUID fraudCheckId, UUID transactionId) {
        restClient.patch()
                .uri("/fraud-checks/{id}/link", fraudCheckId)
                .body(transactionId)
                .retrieve()
                .toBodilessEntity();
    }
}