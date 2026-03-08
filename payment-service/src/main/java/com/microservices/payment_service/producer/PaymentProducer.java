package com.microservices.payment_service.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.microservices.payment_service.model.PaymentSuccessEvent;

@Service
public class PaymentProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentProducer.class);

    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentSuccess(PaymentSuccessEvent event) {
        kafkaTemplate.send("payment-success", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("  [PAYMENT SERVICE] ERROR — Failed to publish payment result for Order ID: {} | Reason: {}",
                                event.getOrderId(), ex.getMessage());
                    } else {
                        log.info("  [PAYMENT SERVICE] SUCCESS — Payment result published to Kafka");
                        log.info("  Topic     : payment-success");
                        log.info("  Order ID  : {}  |  Status: {}", event.getOrderId(), event.getStatus());
                        log.info("  Partition : {}  |  Offset: {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                        log.info("  Waiting for Notification Service to pick it up...");
                    }
                });
    }
}