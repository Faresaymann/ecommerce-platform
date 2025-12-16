package com.example.backend.Order.dto;


import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Valid
public class DirectOrderRequest {

    @NotNull
    private UUID productId;

    @Min(1)
    private int quantity;

    @NotNull
    private String shippingAddress;
}
