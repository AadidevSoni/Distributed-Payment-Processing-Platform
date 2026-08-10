package com.visasim.userservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.visasim.userservice.model.Transaction;
import com.visasim.userservice.repository.TransactionRepository;

@Service
public class TransactionAuditService {

    private final TransactionRepository transactionRepository;

    public TransactionAuditService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedInNewTransaction(Transaction transaction) {
        transaction.markFailed();
        transactionRepository.save(transaction);
    }
}