package com.example.backend.repository;

import com.example.backend.entity.OTP;
import com.example.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTP, Long> {
    Optional<OTP> findByUserAndOtp(Users user, String otp);
    void deleteByUser(Users user);
    void deleteByExpiryDateBefore(LocalDateTime time);

}
