package com.visasim.userservice.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visasim.userservice.exceptions.UserNotFoundException;
import com.visasim.userservice.exceptions.WalletNotFoundException;
import com.visasim.userservice.model.User;
import com.visasim.userservice.model.Wallet;
import com.visasim.userservice.repository.UserRepository;
import com.visasim.userservice.repository.WalletRepository;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Wallet createWalletForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Wallet wallet = new Wallet(user.getId());
        return walletRepository.save(wallet);
    }

    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    @Transactional
    public Wallet credit(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        wallet.credit(amount);
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet debit(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        wallet.debit(amount);
        return walletRepository.save(wallet);
    }
}