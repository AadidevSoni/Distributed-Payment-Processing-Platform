package com.visasim.userservice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.visasim.userservice.client.FraudServiceClient;
import com.visasim.userservice.event.TransactionCompletedEvent;
import com.visasim.userservice.exceptions.DuplicateRequestException;
import com.visasim.userservice.exceptions.FraudBlockedException;
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
    private final IdempotencyService idempotencyService;
    private final FraudServiceClient fraudServiceClient;

    public TransactionService(WalletRepository walletRepository,
                               TransactionRepository transactionRepository,
                               TransactionAuditService transactionAuditService,
                               TransactionEventProducer eventProducer,
                               TransactionTemplate transactionTemplate,
                               IdempotencyService idempotencyService,
                               FraudServiceClient fraudServiceClient) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.transactionAuditService = transactionAuditService;
        this.eventProducer = eventProducer;
        this.transactionTemplate = transactionTemplate;
        this.idempotencyService = idempotencyService;
        this.fraudServiceClient = fraudServiceClient;
    }

    public Transaction transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String idempotencyKey) {

        if (!idempotencyService.markIfFirstUse(idempotencyKey)) {
            throw new DuplicateRequestException(idempotencyKey);
        }

        FraudServiceClient.EvaluateResponse fraudCheck = fraudServiceClient.evaluate(fromWalletId, toWalletId, amount);

        if ("BLOCK".equals(fraudCheck.decision())) {
            throw new FraudBlockedException(fraudCheck.reasons());
        }

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

        fraudServiceClient.linkTransaction(fraudCheck.id(), transaction.getId());

        return transaction;
    }

    public Transaction getById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    public List<Transaction> getRecentHistory(UUID walletId) {
        return transactionRepository.findTop20ByFromWalletIdOrderByCreatedAtDesc(walletId);
    }
}