package com.example.backend.payment.repository;

import com.example.backend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndStatus(Long orderId, Payment.Status status);
    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findByStatusAndCreatedAtBefore(Payment.Status status, OffsetDateTime cutoff);
}