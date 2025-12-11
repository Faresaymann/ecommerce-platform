package com.example.backend.Auth.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private String message;
    private String verificationToken;

    // for messaging and error handling
    public RegisterResponse(String message){
        this.message = message;
    }
}
