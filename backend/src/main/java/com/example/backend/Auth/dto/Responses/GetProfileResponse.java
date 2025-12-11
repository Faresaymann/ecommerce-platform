package com.example.backend.Auth.dto.Responses;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetProfileResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
}
