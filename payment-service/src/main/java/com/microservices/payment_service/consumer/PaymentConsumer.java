package com.microservices.payment_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.microservices.payment_service.model.OrderCreatedEvent;
import com.microservices.payment_service.model.PaymentSuccessEvent;
import com.microservices.payment_service.producer.PaymentProducer;

@Service
public class PaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);

    private final PaymentProducer producer;

    public PaymentConsumer(PaymentProducer producer) {
        this.producer = producer;
    }

    @KafkaListener(topics = "order-created", groupId = "payment-group")
    public void processPayment(OrderCreatedEvent event) {
        log.info("================================================");
        log.info("  PAYMENT SERVICE — Order Event Received");
        log.info("  Order ID  : {}", event.getOrderId());
        log.info("  Product   : {}", event.getProduct());
        log.info("  Quantity  : {}", event.getQuantity());
        log.info("  Processing payment...");

        PaymentSuccessEvent success = new PaymentSuccessEvent();
        success.setOrderId(event.getOrderId());
        success.setStatus("PAID");

        producer.sendPaymentSuccess(success);

        log.info("  Payment Status : PAID");
        log.info("  Publishing result to 'payment-success' topic...");
        log.info("================================================");
    }
}