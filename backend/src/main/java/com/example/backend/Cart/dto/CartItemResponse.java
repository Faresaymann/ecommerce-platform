package com.example.backend.Cart.dto;

import lombok.*;

import java.math.*;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {
    private UUID productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}