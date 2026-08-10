package com.visasim.userservice.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visasim.userservice.exceptions.WalletNotFoundException;
import com.visasim.userservice.model.Transaction;
import com.visasim.userservice.model.Wallet;
import com.visasim.userservice.repository.TransactionRepository;
import com.visasim.userservice.repository.WalletRepository;

@Service
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAuditService transactionAuditService;

    public TransactionService(WalletRepository walletRepository,
                               TransactionRepository transactionRepository,
                               TransactionAuditService transactionAuditService) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.transactionAuditService = transactionAuditService;
    }

    @Transactional
    public Transaction transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        Transaction transaction = new Transaction(fromWalletId, toWalletId, amount);
        transactionRepository.save(transaction);

        try {
            Wallet fromWallet = walletRepository.findById(fromWalletId)
                    .orElseThrow(() -> new WalletNotFoundException(fromWalletId));
            Wallet toWallet = walletRepository.findById(toWalletId)
                    .orElseThrow(() -> new WalletNotFoundException(toWalletId));

            fromWallet.debit(amount);
            toWallet.credit(amount);

            walletRepository.save(fromWallet);
            walletRepository.save(toWallet);

            transaction.markCompleted();
            return transactionRepository.save(transaction);

        } catch (RuntimeException ex) {
            transactionAuditService.markFailedInNewTransaction(transaction);
            throw ex;
        }
    }

    public Transaction getById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new com.visasim.userservice.exceptions.TransactionNotFoundException(id));
    }
}