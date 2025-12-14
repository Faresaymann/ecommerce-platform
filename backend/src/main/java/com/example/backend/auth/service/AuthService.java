package com.example.backend.auth.service;

import com.example.backend.auth.dto.Requests.*;
import com.example.backend.auth.dto.Responses.*;
import com.example.backend.entity.Users;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public interface AuthService {

    //Users register(SignUpRequest signUpRequest);

    RegisterResponse register(SignUpRequest signUpRequest , MultipartFile file) throws IOException;

    LoginResponse login(SignInRequest signInRequest);

    UpdateProfileResponse updateProfile(String userEmail, String firstName, String lastName,  MultipartFile file) throws IOException;

    GetProfileResponse getUserProfile(String email);

    MessageResponse DeleteCurrentUser(String email);

    UpdateEmailRequest requestEmailUpdate(String currentEmail, String newEmail);

    UpdateEmailResponse verifyEmailUpdate(String tokenStr);

    ForgetPasswordResponse forgotPassword(String email);
    MessageResponse resetPassword(ResetPasswordRequest request);
    JwtAuthenticationResponse refreshToken(RefreshTokenReq refreshTokenReq);

    MessageResponse updatePassword(UpdatePasswordRequest request, String currentUserEmail);


    MessageResponse verifyEmail(String token);


    //dev only for testing
    //String findTokenByEmail(String email);
    List<Users> getAllUsers();

}
