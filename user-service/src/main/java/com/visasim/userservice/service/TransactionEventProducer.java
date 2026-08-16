package com.visasim.userservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.visasim.userservice.event.TransactionCompletedEvent;

@Service
public class TransactionEventProducer {

    private static final String TOPIC = "transaction-events";

    private final KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransactionCompleted(TransactionCompletedEvent event) {
        // Key by fromWalletId so all events for the same wallet
        // land on the same partition, preserving order per-wallet.
        kafkaTemplate.send(TOPIC, event.fromWalletId().toString(), event);
    }
}