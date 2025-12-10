package com.example.backend.Auth.service;

import com.example.backend.Auth.dto.*;
import com.example.backend.entity.Users;

public interface AuthService {

    Users register(SignUpRequest signUpRequest);

    JwtAuthenticationResponse login(SignInRequest signInRequest);

    JwtAuthenticationResponse refreshToken(RefreshTokenReq refreshTokenReq);

    void sendResetPasswordEmail(ResetPasswordRequest resetPasswordRequest);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest, String email);

    Users updateProfile(UpdateProfileRequest updateProfileRequest, String email);

    void verifyEmail(EmailVerificationRequest emailVerificationRequest);

}
