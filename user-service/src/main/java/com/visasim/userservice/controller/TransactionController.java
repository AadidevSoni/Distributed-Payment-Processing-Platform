package com.visasim.userservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visasim.userservice.dto.TransactionHistoryResponse;
import com.visasim.userservice.dto.TransactionResponse;
import com.visasim.userservice.dto.TransferRequest;
import com.visasim.userservice.model.Transaction;
import com.visasim.userservice.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request) {

        Transaction transaction = transactionService.transfer(
                request.fromWalletId(),
                request.toWalletId(),
                request.amount(),
                request.idempotencyKey());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TransactionResponse.fromTransaction(transaction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable UUID id) {

        Transaction transaction = transactionService.getById(id);

        return ResponseEntity.ok(
                TransactionResponse.fromTransaction(transaction));
    }

    @GetMapping("/history/{walletId}")
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactionHistory(
            @PathVariable UUID walletId) {

        List<TransactionHistoryResponse> history = transactionService.getRecentHistory(walletId)
                .stream()
                .map(TransactionHistoryResponse::fromTransaction)
                .toList();

        return ResponseEntity.ok(history);
    }
}