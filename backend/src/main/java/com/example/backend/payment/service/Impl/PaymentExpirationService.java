package com.example.backend.payment.service.Impl;

import com.example.backend.payment.entity.Payment;
import com.example.backend.payment.repository.PaymentRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;


/**
 * Check for expired payments every minute.(after 60 minutes the link(of payment) will be expired )
 */
@Service
@RequiredArgsConstructor
public class PaymentExpirationService {

    private final PaymentRepository paymentRepository;

    @Value("${payment.session.ttl.minutes:60}")
    private long ttlMinutes;


    @Scheduled(cron = "${payment.expire-check-cron}") // every 1 minute
    public void expireOldPayments() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(ttlMinutes);
        List<Payment> toExpire = paymentRepository.findByStatusAndCreatedAtBefore(Payment.Status.CREATED, cutoff);

        for (Payment p : toExpire) {
            try {
                // double-check with Stripe: if user already paid, confirm and continue
                Session session = Session.retrieve(p.getSessionId());
                boolean paid = false;
                if (session.getPaymentIntent() != null) {
                    PaymentIntent pi = PaymentIntent.retrieve(session.getPaymentIntent());
                    paid = "succeeded".equals(pi.getStatus());
                } else if ("paid".equals(session.getPaymentStatus())) {
                    paid = true;
                }

                if (paid) {
                    p.setStatus(Payment.Status.PAID);
                    p.setPaidAt(OffsetDateTime.now());
                    paymentRepository.save(p);
                    continue;
                }

                // not paid => expire locally
                p.setStatus(Payment.Status.FAILED);
                paymentRepository.save(p);

                // OPTIONAL: cancel the PaymentIntent to avoid later use
                if (session.getPaymentIntent() != null) {
                    try {
                        PaymentIntent pi = PaymentIntent.retrieve(session.getPaymentIntent());
                        // only cancel if it's in a cancelable state; this call may throw if not allowed
                        pi.cancel();
                    } catch (Exception e) {
                        // log & continue — cancellation not required
                    }
                }
            } catch (Exception ex) {
                // log error and continue with next
            }
        }
    }
}
