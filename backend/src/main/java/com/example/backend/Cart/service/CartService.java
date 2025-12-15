package com.example.backend.Cart.service;

import com.example.backend.Cart.dto.*;
import com.example.backend.auth.dto.Responses.MessageResponse;

import java.util.UUID;

public interface CartService {
    CartResponse addToCart(String userEmail, UUID productId, Integer quantity);
    CartResponse updateQuantity(String userEmail, UUID productId, Integer quantity);
    CartResponse removeFromCart(String userEmail, UUID productId);
    CartResponse getCart(String userEmail);
    MessageResponse clearCart(String userEmail);
}
