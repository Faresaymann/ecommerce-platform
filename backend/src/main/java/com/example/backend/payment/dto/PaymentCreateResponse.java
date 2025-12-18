package com.example.backend.payment.dto;

import lombok.*;

/**
 * Response returned when creating a Stripe Checkout session.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCreateResponse {
    private String sessionId;
    private String url;


    public static PaymentCreateResponse of(String sessionId, String url) {
        return new PaymentCreateResponse(sessionId, url);
    }

}