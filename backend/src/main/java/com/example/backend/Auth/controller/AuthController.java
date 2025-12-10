package com.example.backend.Auth.controller;
//All endpoints (register, login, refresh, reset password, etc.)


import com.example.backend.entity.Users;
import com.example.backend.entity.VerificationToken;
import com.example.backend.repository.UsersRepo;
import com.example.backend.repository.VerificationTokenRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController("api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final VerificationTokenRepo verificationTokenRepo;
    private final UsersRepo usersRepo;

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        VerificationToken verificationToken = verificationTokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (verificationToken.isUsed() || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token invalid or expired");
        }

        Users user = verificationToken.getUser();
        user.setEnabled(true);
        usersRepo.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepo.save(verificationToken);

        return ResponseEntity.ok("Email verified successfully!");
    }

}
