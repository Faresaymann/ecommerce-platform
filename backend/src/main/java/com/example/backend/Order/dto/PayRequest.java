package com.example.backend.Order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayRequest {
    @NotBlank
    private String phoneNumber;

    // idempotency key to prevent duplicate payment attempts.
    private String idempotencyKey;
}