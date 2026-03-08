package com.microservices.order_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservices.order_service.model.Order;
import com.microservices.order_service.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // POST /orders — place a new order
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        service.createOrder(order);
        return ResponseEntity.ok("Order [" + order.getOrderId() + "] accepted and published to Kafka");
    }

    // GET /orders — list all orders placed in this session
    @GetMapping
    public ResponseEntity<List<Order>> getOrders() {
        return ResponseEntity.ok(service.getOrders());
    }
}