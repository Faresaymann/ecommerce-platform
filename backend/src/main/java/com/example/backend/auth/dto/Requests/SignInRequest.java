package com.example.backend.auth.dto.Requests;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignInRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}