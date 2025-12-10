package com.example.backend.Auth.service;


import com.example.backend.entity.Users;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.util.List;

public interface JWTservice {
    String generateToken(Users user);
    String generateRefreshToken(Users user);
    boolean validateToken(String token, UserDetails userDetails);
    String extractUsername(String token);
    List<String> extractRoles(String token);
}
