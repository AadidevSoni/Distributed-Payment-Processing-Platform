package com.visasim.notificationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.visasim.notificationservice.event.TransactionCompletedEvent;

@Component
public class NotificationKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationKafkaConsumer.class);

    @KafkaListener(
            topics = "transaction-events",
            groupId = "notification-service-v2",
            concurrency = "3"
    )
    public void consume(TransactionCompletedEvent event) {

        log.info(
                "Payment completed: {} | From: {} | To: {} | Amount: {} | Status: {}",
                event.transactionId(),
                event.fromWalletId(),
                event.toWalletId(),
                event.amount(),
                event.status()
        );
    }
}