package com.visasim.userservice.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.visasim.userservice.event.TransactionCompletedEvent;

@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);

    @KafkaListener(topics = "transaction-events", groupId = "user-service-group")
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        log.info("Received transaction event: id={}, from={}, to={}, amount={}, status={}",
                event.transactionId(), event.fromWalletId(), event.toWalletId(),
                event.amount(), event.status());

        // Stand-in for what will become the real Notification Service.
        // In a true microservices split (Milestone 11), this listener
        // moves into its own separate service entirely.
    }
}