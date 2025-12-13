package com.example.backend.auth.dto.Responses;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private String accessToken; // JWT token
    private String refreshToken;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
}
