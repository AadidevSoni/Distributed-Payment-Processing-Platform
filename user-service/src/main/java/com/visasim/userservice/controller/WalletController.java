package com.visasim.userservice.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visasim.userservice.dto.CreditRequest;
import com.visasim.userservice.dto.DebitRequest;
import com.visasim.userservice.dto.WalletResponse;
import com.visasim.userservice.model.Wallet;
import com.visasim.userservice.service.WalletService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<WalletResponse> createWallet(@PathVariable UUID userId) {
        Wallet wallet = walletService.createWalletForUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(WalletResponse.fromWallet(wallet));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(WalletResponse.fromWallet(wallet));
    }

    @PostMapping("/{walletId}/credit")
    public ResponseEntity<WalletResponse> credit(
            @PathVariable UUID walletId,
            @Valid @RequestBody CreditRequest request) {
        Wallet wallet = walletService.credit(walletId, request.amount());
        return ResponseEntity.ok(WalletResponse.fromWallet(wallet));
    }

    @PostMapping("/{walletId}/debit")
    public ResponseEntity<WalletResponse> debit(
            @PathVariable UUID walletId,
            @Valid @RequestBody DebitRequest request) {
        Wallet wallet = walletService.debit(walletId, request.amount());
        return ResponseEntity.ok(WalletResponse.fromWallet(wallet));
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(@PathVariable UUID walletId) {
        walletService.deleteWallet(walletId);
        return ResponseEntity.noContent().build();
    }
}