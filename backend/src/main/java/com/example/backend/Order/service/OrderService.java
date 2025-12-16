package com.example.backend.Order.service;

import com.example.backend.Order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    Order createOrderFromCart(String userEmail, String shippingAddress);
    Order createDirectOrder(String userEmail, UUID productId, int quantity, String shippingAddress);
    Order getOrderById(Long id, String userEmail);
    Page<Order> getOrdersForUser(String userEmail, Pageable pageable);
    void cancelOrder(Long orderId, String userEmail);
}