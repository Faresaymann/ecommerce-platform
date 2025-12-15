package com.example.backend.Cart.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemRequest {
    private UUID productId;
    private Integer quantity;
}