package com.example.backend.repository;


import com.example.backend.entity.Users;
import com.example.backend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepo extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(Users user);
    void deleteByUser(Users user);
}
