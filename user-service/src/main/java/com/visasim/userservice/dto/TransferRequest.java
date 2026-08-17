package com.visasim.userservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(

        @NotNull(message = "fromWalletId is required")
        UUID fromWalletId,

        @NotNull(message = "toWalletId is required")
        UUID toWalletId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
        BigDecimal amount,

        @NotBlank(message = "idempotencyKey is required")
        String idempotencyKey
) {}