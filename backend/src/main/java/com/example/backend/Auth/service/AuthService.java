package com.example.backend.Auth.service;

import com.example.backend.Auth.dto.Requests.*;
import com.example.backend.Auth.dto.Responses.*;
import com.example.backend.entity.Users;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AuthService {

    //Users register(SignUpRequest signUpRequest);

    RegisterResponse register(SignUpRequest signUpRequest , MultipartFile file) throws IOException;

    LoginResponse login(SignInRequest signInRequest);

    UpdateProfileResponse updateProfile(String userEmail, String firstName, String lastName,  MultipartFile file) throws IOException;

    GetProfileResponse getUserProfile(String email);

    DeleteResponse DeleteCurrentUser(String email);

    UpdateEmailRequest requestEmailUpdate(String currentEmail, String newEmail);
    UpdateEmailResponse verifyEmailUpdate(String tokenStr);

    JwtAuthenticationResponse refreshToken(RefreshTokenReq refreshTokenReq);

    void sendResetPasswordEmail(ResetPasswordRequest resetPasswordRequest);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest, String email);

    RegisterResponse verifyEmail(String token);


    //dev only for testing
    //String findTokenByEmail(String email);
    List<Users> getAllUsers();

}
