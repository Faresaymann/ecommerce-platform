package com.example.backend.payment.Controller;


import com.example.backend.payment.dto.PaymentConfirmDto;
import com.example.backend.payment.dto.PaymentCreateResponse;
import com.example.backend.payment.dto.RefundRequest;
import com.example.backend.payment.exception.OrderPaymentNotAllowedException;
import com.example.backend.payment.exception.PaymentNotFoundException;
import com.example.backend.payment.exception.StripeOperationException;
import com.example.backend.payment.service.PaymentService;
import com.stripe.model.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    /**
     * Create a Stripe checkout session for a specific order.
     *
     * @param orderId the ID of the order to pay
     * @param authentication Spring Security authentication object (used to get user email)
     * @return PaymentCreateResponse DTO containing the Stripe session ID and URL
     * @throws OrderPaymentNotAllowedException if the order does not exist or does not belong to the authenticated user
     * @throws StripeOperationException if there is an error communicating with Stripe
     */
    @PostMapping("/create/{orderId}")
    public PaymentCreateResponse createPayment(
            @PathVariable Long orderId,
            Authentication authentication) {

        return paymentService.createCheckoutSessionForOrder(
                orderId,
                authentication.getName()
        );
    }


    /**
     * Confirm the payment status of a Stripe checkout session.
     *
     * @param sessionId the Stripe checkout session ID to confirm
     * @param authentication Spring Security authentication object (used to get user email)
     * @return PaymentConfirmDto indicating whether the payment was completed successfully or is still pending
     * @throws PaymentNotFoundException if the payment session cannot be found
     * @throws OrderPaymentNotAllowedException if the order does not belong to the authenticated user
     * @throws StripeOperationException if there is an error communicating with Stripe
     */

    @GetMapping("/confirm")
    public PaymentConfirmDto confirmPayment(
            @RequestParam("session_id") String sessionId,
            Authentication authentication) {

        return paymentService.confirmPaymentBySessionId(
                sessionId,
                authentication.getName()
        );
    }

    /**
     * Process a refund for a paid order.
     *
     * Allows the authenticated user to request a full or partial refund
     * for an order they own. The refund is processed through Stripe, and
     * the local payment record is updated accordingly.
     *
     * @param req the refund request containing order ID and optional refund amount
     * @param auth Spring Security authentication object (used to get user email)
     * @return the Stripe Refund object representing the processed refund
     * @throws PaymentNotFoundException if no paid payment exists for the order
     * @throws OrderPaymentNotAllowedException if the order does not belong to the authenticated user
     * @throws StripeOperationException if there is an error communicating with Stripe
     */

    @PostMapping("/refund")
    public ResponseEntity<?> refund(@RequestBody RefundRequest req, Authentication auth) {
        // validate ownership
       Refund refund = paymentService.refundPaymentForOrder(req, auth.getName());
        return ResponseEntity.ok(Map.of("refundId", refund.getId(), "status", refund.getStatus()));
    }
}
