package com.example.backend.auth.dto.Responses;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForgetPasswordResponse {
    private String message;
    private String otp;
}
