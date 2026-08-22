package com.visasim.fraudservice.client;

import com.visasim.fraudservice.dto.TransactionHistoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${user-service.base-url}") String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public List<TransactionHistoryResponse> getTransactionHistory(
            UUID walletId) {

        return restClient
                .get()
                .uri("/transactions/history/{walletId}", walletId)
                .retrieve()
                .body(new ParameterizedTypeReference<
                        List<TransactionHistoryResponse>>() {});
    }
}