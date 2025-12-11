package com.example.backend.Auth.dto.Requests;

import lombok.Data;

@Data
public class EmailVerificationRequest {
    private String token;
}