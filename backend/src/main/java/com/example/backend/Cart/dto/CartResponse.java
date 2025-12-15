package com.example.backend.Cart.dto;
import lombok.*;

import java.util.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CartResponse {
    private UUID cartId;
    private List<CartItemResponse> items;
    private Double totalPrice;
}
