package com.microservices.notification_service.model;

import lombok.Data;

@Data
public class PaymentSuccessEvent {

    private String orderId;
    private String status;

}