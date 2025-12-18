package com.example.backend.payment.exception;

public class OrderPaymentNotAllowedException extends RuntimeException {
    public OrderPaymentNotAllowedException(String message) {
        super(message);
    }
}
