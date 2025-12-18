package com.example.backend.payment.service.Impl;

import com.example.backend.Order.entity.Order;
import com.example.backend.Order.entity.OrderStatus;
import com.example.backend.Order.repository.OrderRepository;
import com.example.backend.payment.dto.PaymentConfirmDto;
import com.example.backend.payment.dto.PaymentCreateResponse;
import com.example.backend.payment.dto.RefundRequest;
import com.example.backend.payment.entity.Payment;
import com.example.backend.payment.exception.OrderPaymentNotAllowedException;
import com.example.backend.payment.exception.PaymentNotFoundException;
import com.example.backend.payment.exception.StripeOperationException;
import com.example.backend.payment.repository.PaymentRepository;
import com.example.backend.payment.service.PaymentService;
import com.example.backend.payment.utils.StripeUtils;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${stripe.secretKey}")
    private String stripeApiKey;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    //---------------------------------------------------createCheckoutSessionForOrder--------------------------------------------------//
    /**
     * Create a checkout session for the given orderId, verifying that this is the owner's order
     * matches the order's owner email. Returns the created Session (caller extracts id + url).
     */
    public PaymentCreateResponse createCheckoutSessionForOrder(Long orderId, String userEmail) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderPaymentNotAllowedException("Order not found: " + orderId));

        if (order.getUser() == null || order.getUser().getEmail() == null ||
                !order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new OrderPaymentNotAllowedException("Order does not belong to authenticated user");
        }

        BigDecimal amountInCentsBD = order.getTotalAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
        long amountInCents = amountInCentsBD.longValueExact();

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Order #" + order.getId())
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(amountInCents)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(priceData)
                        .setQuantity(1L)
                        .build();

        String successUrl = appBaseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl  = appBaseUrl + "/payment/cancel";

        Session session;
        try {
            session = Session.create(
                    SessionCreateParams.builder()
                            .addLineItem(lineItem)
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(successUrl)
                            .setCancelUrl(cancelUrl)
                            .setCustomerEmail(userEmail)
                            .putMetadata("order_id", String.valueOf(order.getId()))
                            .build()
            );
        } catch (StripeException e) {
            throw new StripeOperationException("Stripe error while creating checkout session", e);
        }


        Payment payment = new Payment(order.getId(), session.getId());
        payment.setStatus(Payment.Status.CREATED);
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setExpiresAt(payment.getCreatedAt().plusMinutes(60)); // TTL = 60 minutes (after that the transaction will be cancelled)
        paymentRepository.save(payment);

        order.setStatus(OrderStatus.DELIVERED); // to avoid confusion (BE happy: D)

        return  PaymentCreateResponse.of(session.getId(), session.getUrl());
    }

    //---------------------------------------------------confirmPaymentBySessionId--------------------------------------------------//

    /**
     * Confirm payment by calling Stripe to retrieve the session/paymentIntent status.
     * Also verifies the session's customer email or order owner equals authenticated user.
     * If paid -> mark the order as PAID and update the Payment record.
     *
     * Throws:
     * - PaymentNotFoundException when we can't map session -> order
     * - OrderPaymentNotAllowedException for ownership / order problems
     * - PaymentNotCompletedException when the payment hasn't succeeded yet
     * - StripeOperationException wrapping Stripe SDK errors
     */
    @Override
    public PaymentConfirmDto confirmPaymentBySessionId(String sessionId, String userEmail) {

        Session session;
        PaymentIntent pi = null; // keep PaymentIntent reference for later DB persistence
        try {
            session = Session.retrieve(sessionId);
        } catch (StripeException e) {
            throw new StripeOperationException("Stripe error while confirming payment", e);
        }

        String sessionEmail = session.getCustomerEmail();
        if (sessionEmail != null && !sessionEmail.equalsIgnoreCase(userEmail)) {
            throw new OrderPaymentNotAllowedException("Authenticated user does not match session email");
        }

        String orderIdStr = session.getMetadata() != null
                ? session.getMetadata().get("order_id")
                : null;

        if (orderIdStr == null) {
            Payment payment = paymentRepository.findBySessionId(sessionId)
                    .orElseThrow(() ->
                            new PaymentNotFoundException("Payment not found for session: " + sessionId));
            orderIdStr = String.valueOf(payment.getOrderId());
        }

        Order order = orderRepository.findById(Long.valueOf(orderIdStr))
                .orElseThrow(() ->
                        new OrderPaymentNotAllowedException("Order not found"));

        if (!order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new OrderPaymentNotAllowedException("Order does not belong to authenticated user");
        }

        boolean paid = false;
        try {
            if (session.getPaymentIntent() != null) {
                // retrieve PaymentIntent once and keep reference
                pi = PaymentIntent.retrieve(session.getPaymentIntent());
                paid = "succeeded".equals(pi.getStatus());
            } else if ("paid".equals(session.getPaymentStatus())) {
                paid = true;
            }
        } catch (StripeException e) {
            throw new StripeOperationException("Stripe error while checking payment status", e);
        }

        if (!paid) {
            return PaymentConfirmDto.pending("Payment not completed yet");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        }

        String piId = session.getPaymentIntent();
        PaymentIntent finalPi = pi;
        paymentRepository.findBySessionId(sessionId).ifPresent(p -> {
            // store PaymentIntent ID
            if (piId != null && !piId.isBlank()) {
                p.setPaymentIntentId(piId);
            }

            // mark paid
            p.setStatus(Payment.Status.PAID);
            p.setPaidAt(java.time.OffsetDateTime.now());

            // application_fee_amount is in cents (Long) when using Connect
            if (finalPi != null) {
                Long applicationFee = finalPi.getApplicationFeeAmount(); // may be null
                Long amount = finalPi.getAmount(); // total amount in cents

                // sellerStripeAccountId -> transfer_data.destination
                if (finalPi.getTransferData() != null) {
                    String dest = finalPi.getTransferData().getDestination();
                    p.setSellerStripeAccountId(dest);
                }

                // platform fee (store as cents)
                if (applicationFee != null) {
                    p.setPlatformFeeAmount(applicationFee);
                }

                // sellerAmount = total - platformFee
                if (amount != null) {
                    long fee = (applicationFee != null ? applicationFee : 0L);
                    p.setSellerAmount(amount - fee);
                }
            }

            paymentRepository.save(p);
        });

        return PaymentConfirmDto.ok("Payment confirmed successfully");
    }

    //---------------------------------------------------refundPaymentForOrder--------------------------------------------------//

    /**
     * Refund a completed payment for an order using Stripe.
     * Supports full or partial refunds based on the provided amount.
     *
     * The service:
     * - Verifies a PAID payment exists for the order
     * - Ensures the order belongs to the authenticated user
     * - Calls Stripe to create the refund
     * - Updates the local payment status to REFUNDED
     *
     * Throws:
     * - PaymentNotFoundException when no paid payment exists for the order
     * - OrderPaymentNotAllowedException when the order does not belong to the user
     * - IllegalArgumentException when the refund amount is invalid
     * - StripeOperationException when Stripe refund creation fails
     *
     * @param request the refund request containing order ID and optional refund amount
     * @param userEmail the authenticated user's email
     * @return the created Stripe Refund object
     */
    @Override
    public com.stripe.model.Refund refundPaymentForOrder(RefundRequest request, String userEmail) {
        // find the paid Payment for the order
        Payment payment = paymentRepository.findByOrderIdAndStatus(request.getOrderId(), Payment.Status.PAID)
                .orElseThrow(() -> new PaymentNotFoundException("Paid payment not found for order id=" + request.getOrderId()));

        // check ownership
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderPaymentNotAllowedException("Order not found id=" + request.getOrderId()));
        if (!order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new OrderPaymentNotAllowedException("User cannot refund this order");
        }

        String paymentIntentId = payment.getPaymentIntentId();
        log.debug("Refund requested for orderId={} paymentId={} paymentIntentId={}",
                request.getOrderId(), payment.getId(), paymentIntentId);

        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new PaymentNotFoundException("PaymentIntent ID missing for payment id=" + payment.getId());
        }

        try {
            com.stripe.param.RefundCreateParams.Builder builder = com.stripe.param.RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId);

            // validate and set partial refund amount if provided
            if (request.getAmount() != null) {
                BigDecimal originalAmount = order.getTotalAmount();
                if (originalAmount != null && request.getAmount().compareTo(originalAmount) > 0) {
                    throw new IllegalArgumentException("Refund amount cannot be greater than original amount");
                }
                long cents = StripeUtils.amountToCents(request.getAmount()); // your util
                builder.setAmount(cents);
            }

            com.stripe.model.Refund stripeRefund = com.stripe.model.Refund.create(builder.build());

            // update local payment status and persisted fields
            payment.setStatus(Payment.Status.REFUNDED);
            order.setStatus(OrderStatus.REFUNDED);
            payment.setRefundedAt(java.time.OffsetDateTime.now());
            paymentRepository.save(payment);

            return stripeRefund;
        } catch (com.stripe.exception.StripeException e) {
            log.error("Stripe refund failed for paymentId={} paymentIntentId={}: {}", payment.getId(), paymentIntentId, e.getMessage(), e);
            throw new StripeOperationException("Stripe refund failed: " + e.getMessage(), e);
        }
    }

}
