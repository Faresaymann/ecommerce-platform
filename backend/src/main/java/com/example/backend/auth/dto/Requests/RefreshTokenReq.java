package com.example.backend.auth.dto.Requests;

import lombok.Data;

@Data
public class RefreshTokenReq {
    private String token;
}
