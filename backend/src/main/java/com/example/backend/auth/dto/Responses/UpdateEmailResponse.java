package com.example.backend.auth.dto.Responses;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmailResponse {
    private String message;
    private String newEmail;
}
