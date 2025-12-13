package com.example.backend.auth.dto.Requests;

import lombok.Data;

@Data
public class SignInRequest {
    private String email;
    private String password;
}