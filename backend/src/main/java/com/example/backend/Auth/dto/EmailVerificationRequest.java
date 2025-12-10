package com.example.backend.Auth.dto;

import lombok.Data;

@Data
public class EmailVerificationRequest {
    private String token;
}