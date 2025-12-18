package com.example.backend.payment.dto;


import lombok.*;

/**
 * Response returned when confirming a session/payment.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentConfirmDto {
    private String message;
    private boolean paid;

    public static PaymentConfirmDto ok(String message) {
        return new PaymentConfirmDto(message, true);
    }

    public static PaymentConfirmDto pending(String message) {
        return new PaymentConfirmDto(message, false);
    }
}