package com.microservices.order_service.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microservices.order_service.model.Order;
import com.microservices.order_service.producer.OrderProducer;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderProducer producer;
    private final List<Order> orders = Collections.synchronizedList(new ArrayList<>());

    public OrderService(OrderProducer producer) {
        this.producer = producer;
    }

    public void createOrder(Order order) {
        log.info("================================================");
        log.info("  ORDER SERVICE — New Order Received");
        log.info("  Order ID  : {}", order.getOrderId());
        log.info("  Product   : {}", order.getProduct());
        log.info("  Quantity  : {}", order.getQuantity());
        log.info("================================================");
        orders.add(order);
        producer.sendOrder(order);
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }
}