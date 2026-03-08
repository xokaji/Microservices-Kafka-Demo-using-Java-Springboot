package com.microservices.order_service.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.microservices.order_service.model.Order;

@Service
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    private final KafkaTemplate<String, Order> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(Order order) {
        kafkaTemplate.send("order-created", order)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("  [ORDER SERVICE] ERROR — Failed to publish Order ID: {} | Reason: {}",
                                order.getOrderId(), ex.getMessage());
                    } else {
                        log.info("  [ORDER SERVICE] SUCCESS — Order published to Kafka");
                        log.info("  Topic     : order-created");
                        log.info("  Order ID  : {}", order.getOrderId());
                        log.info("  Partition : {}  |  Offset: {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                        log.info("  Waiting for Payment Service to pick it up...");
                    }
                });
    }
}