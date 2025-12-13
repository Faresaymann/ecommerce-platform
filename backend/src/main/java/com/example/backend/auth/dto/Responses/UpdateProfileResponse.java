package com.example.backend.auth.dto.Responses;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileResponse {
    private String message;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
}
