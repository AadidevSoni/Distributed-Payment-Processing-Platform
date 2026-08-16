package com.visasim.userservice.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.visasim.userservice.event.TransactionCompletedEvent;
import com.visasim.userservice.exceptions.TransactionNotFoundException;
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
    private final TransactionEventProducer eventProducer;
    private final TransactionTemplate transactionTemplate;

    public TransactionService(WalletRepository walletRepository,
                               TransactionRepository transactionRepository,
                               TransactionAuditService transactionAuditService,
                               TransactionEventProducer eventProducer,
                               TransactionTemplate transactionTemplate) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.transactionAuditService = transactionAuditService;
        this.eventProducer = eventProducer;
        this.transactionTemplate = transactionTemplate;
    }

    public Transaction transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {

        Transaction transaction = transactionTemplate.execute(status -> {

            Transaction tx = new Transaction(fromWalletId, toWalletId, amount);
            transactionRepository.save(tx);

            try {
                Wallet fromWallet = walletRepository.findById(fromWalletId)
                        .orElseThrow(() -> new WalletNotFoundException(fromWalletId));
                Wallet toWallet = walletRepository.findById(toWalletId)
                        .orElseThrow(() -> new WalletNotFoundException(toWalletId));

                fromWallet.debit(amount);
                toWallet.credit(amount);

                walletRepository.save(fromWallet);
                walletRepository.save(toWallet);

                tx.markCompleted();
                return transactionRepository.save(tx);

            } catch (RuntimeException ex) {
                transactionAuditService.markFailedInNewTransaction(tx);
                throw ex;
            }
        });

        // Publish only AFTER the database transaction has fully committed.
        // If the transfer failed, this line is never reached (the exception
        // propagated out of transactionTemplate.execute() above).
        eventProducer.publishTransactionCompleted(new TransactionCompletedEvent(
                transaction.getId(),
                transaction.getFromWalletId(),
                transaction.getToWalletId(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getCreatedAt()
        ));

        return transaction;
    }

    public Transaction getById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }
}