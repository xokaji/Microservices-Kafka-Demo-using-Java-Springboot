package com.microservices.notification_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.microservices.notification_service.model.PaymentSuccessEvent;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "payment-success", groupId = "notification-group")
    public void sendNotification(PaymentSuccessEvent event) {
        log.info("================================================");
        log.info("  NOTIFICATION SERVICE — Payment Event Received");
        log.info("  Order ID  : {}", event.getOrderId());
        log.info("  Status    : {}", event.getStatus());
        log.info("  Sending notification to customer...");
        log.info("  Notification sent! — Order {} has been PAID and confirmed.", event.getOrderId());
        log.info("================================================");
    }
}