package com.example.backend.auth.dto.Requests;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;


}
