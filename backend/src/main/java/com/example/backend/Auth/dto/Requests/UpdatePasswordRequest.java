package com.example.backend.Auth.dto.Requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePasswordRequest {
    private String oldPassword;
    private String newPassword;
}