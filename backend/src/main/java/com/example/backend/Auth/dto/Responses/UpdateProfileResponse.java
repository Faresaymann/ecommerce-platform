package com.example.backend.Auth.dto.Responses;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileResponse {
    private String message;
    private String profileImageUrl;
}
