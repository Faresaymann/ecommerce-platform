package com.example.backend.auth.dto.Responses;

import com.example.backend.entity.Role;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String message;
    private String accessToken; // JWT token
    private String refreshToken;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private Role role;
}
