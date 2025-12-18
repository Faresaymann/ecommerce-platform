package com.example.backend.payment.service;
import com.example.backend.payment.dto.*;
import com.stripe.model.Refund;



public interface PaymentService {
    PaymentCreateResponse createCheckoutSessionForOrder(Long orderId, String userEmail);
    PaymentConfirmDto confirmPaymentBySessionId(String sessionId, String userEmail);
    Refund refundPaymentForOrder(RefundRequest request, String userEmail);
}
