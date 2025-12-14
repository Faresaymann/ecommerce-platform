package com.example.backend.scheduler;

import com.example.backend.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OTPCleanupScheduler {

    private final OTPRepository otpRepository;

    // Runs every hour
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
