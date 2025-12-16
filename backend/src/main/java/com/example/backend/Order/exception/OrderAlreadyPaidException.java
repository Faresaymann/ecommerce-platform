package com.example.backend.Order.exception;

public class OrderAlreadyPaidException extends RuntimeException {
    public OrderAlreadyPaidException(Long orderId) {
        super("Order already paid: " + orderId);
    }
}