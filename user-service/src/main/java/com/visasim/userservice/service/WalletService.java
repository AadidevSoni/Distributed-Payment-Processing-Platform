package com.visasim.userservice.service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.visasim.userservice.exceptions.UserNotFoundException;
import com.visasim.userservice.exceptions.WalletHasDependentDataException;
import com.visasim.userservice.exceptions.WalletNotFoundException;
import com.visasim.userservice.model.Transaction;
import com.visasim.userservice.model.User;
import com.visasim.userservice.model.Wallet;
import com.visasim.userservice.repository.TransactionRepository;
import com.visasim.userservice.repository.UserRepository;
import com.visasim.userservice.repository.WalletRepository;

@Service
public class WalletService {

    private static final int MAX_RETRIES = 10;
    private static final long INITIAL_BACKOFF_MS = 10;

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;
    private final TransactionRepository transactionRepository;

    public WalletService(
            WalletRepository walletRepository,
            UserRepository userRepository,
            TransactionTemplate transactionTemplate,
            TransactionRepository transactionRepository) {

        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionTemplate = transactionTemplate;
        this.transactionRepository = transactionRepository;
    }

    public Wallet createWalletForUser(UUID userId) {
        return transactionTemplate.execute(status -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            Wallet wallet = new Wallet(user.getId());

            return walletRepository.save(wallet);
        });
    }

    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    public Wallet credit(UUID walletId, BigDecimal amount) {
        return executeWithRetry(() -> transactionTemplate.execute(status -> {

            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new WalletNotFoundException(walletId));

            wallet.credit(amount);

            return walletRepository.save(wallet);
        }));
    }

    public Wallet debit(UUID walletId, BigDecimal amount) {
        return executeWithRetry(() -> transactionTemplate.execute(status -> {

            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new WalletNotFoundException(walletId));

            wallet.debit(amount);

            return walletRepository.save(wallet);
        }));
    }

    private Wallet executeWithRetry(Supplier<Wallet> operation) {

        long backoff = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

            try {
                return operation.get();

            } catch (ObjectOptimisticLockingFailureException ex) {

                if (attempt == MAX_RETRIES) {
                    throw ex;
                }

                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(
                            "Retry interrupted", e);
                }

                backoff = Math.min(backoff * 2, 200);
            }
        }

        throw new IllegalStateException("Unexpected retry failure");
    }

    public void deleteWallet(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        boolean hasTransactions = transactionRepository.existsByFromWalletIdOrToWalletId(walletId, walletId);
        if (hasTransactions) {
            throw new WalletHasDependentDataException(walletId);
        }

        walletRepository.delete(wallet);
    }
}