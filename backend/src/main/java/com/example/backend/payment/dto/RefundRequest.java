package com.example.backend.payment.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {
    private Long orderId;      // the order to refund
    private BigDecimal amount;// optional partial refund, null = full refund

}
