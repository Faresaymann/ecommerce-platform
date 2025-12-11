package com.example.backend.Auth.service;

import com.example.backend.Auth.dto.Requests.*;
import com.example.backend.Auth.dto.Responses.JwtAuthenticationResponse;
import com.example.backend.Auth.dto.Responses.RegisterResponse;
import com.example.backend.Auth.dto.Responses.UpdateProfileResponse;
import com.example.backend.entity.Users;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AuthService {

    //Users register(SignUpRequest signUpRequest);

    RegisterResponse register(SignUpRequest signUpRequest , MultipartFile file) throws IOException;

    JwtAuthenticationResponse login(SignInRequest signInRequest);

    UpdateProfileResponse updateProfile(String userEmail, String firstName, String lastName, String newEmail, MultipartFile file) throws IOException;

    JwtAuthenticationResponse refreshToken(RefreshTokenReq refreshTokenReq);

    void sendResetPasswordEmail(ResetPasswordRequest resetPasswordRequest);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest, String email);

    RegisterResponse verifyEmail(String token);


    //dev only for testing
    //String findTokenByEmail(String email);
    List<Users> getAllUsers();

}
