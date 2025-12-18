package com.example.backend.payment.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payments")
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @Column(unique = true)
    private String sessionId;

    private Long platformFeeAmount;       // cents
    private Long sellerAmount;            // cents
    private String sellerStripeAccountId;

    public enum Status { CREATED, PAID, FAILED, REFUNDED }

    // store PaymentIntent id (if available) to enable refunds/cancels
    @Column(name = "payment_intent_id" , unique = true)
    private String paymentIntentId;

    @Enumerated(EnumType.STRING)
    private Status status = Status.CREATED;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime paidAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime refundedAt;

    public Payment(Long orderId, String sessionId) {
        this.orderId = orderId;
        this.sessionId = sessionId;
        this.createdAt = OffsetDateTime.now();
    }

}
