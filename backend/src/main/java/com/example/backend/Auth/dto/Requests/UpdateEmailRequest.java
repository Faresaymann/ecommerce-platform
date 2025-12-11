package com.example.backend.Auth.dto.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateEmailRequest {
    private String message;
    private String newEmail;
    private String verificationToken;
}