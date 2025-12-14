package com.example.backend.auth.dto.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateEmailRequest {
    private String message;
    @Email(message = "Invalid email format")
    @NotBlank(message = "New email is required")
    private String newEmail;
    @NotBlank(message = "Verification token is required")
    private String verificationToken;
}