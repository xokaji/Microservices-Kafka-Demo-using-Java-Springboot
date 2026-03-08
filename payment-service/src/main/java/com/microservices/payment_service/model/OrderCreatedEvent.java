package com.microservices.payment_service.model;

import lombok.Data;

@Data
public class OrderCreatedEvent {

    private String orderId;
    private String product;
    private int quantity;

}