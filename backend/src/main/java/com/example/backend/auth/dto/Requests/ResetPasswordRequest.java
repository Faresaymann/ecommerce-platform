package com.example.backend.auth.dto.Requests;

import jakarta.validation.constraints.*;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest {
    private String email;
    @NotBlank(message = "otp is required")
    private String otp;
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;


}
