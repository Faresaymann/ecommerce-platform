package com.example.backend.Auth.service.Impl;

import com.example.backend.Auth.dto.*;
import com.example.backend.Auth.service.AuthService;
import com.example.backend.entity.Role;
import com.example.backend.entity.Users;
import com.example.backend.entity.VerificationToken;
import com.example.backend.repository.UsersRepo;
import com.example.backend.repository.VerificationTokenRepo;
import com.example.backend.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UsersRepo usersRepo;
    private VerificationTokenRepo verificationTokenRepo;
    private EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JWTserviceImpl jwtService;

    @Override
    public Users register(SignUpRequest signUpRequest) {
        Users user = new Users();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setRole(Role.ROLE_USER);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        return usersRepo.save(user);
    }

    @Override
    public JwtAuthenticationResponse login(SignInRequest signInRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getEmail(),signInRequest.getPassword()));
        var user = usersRepo.findByEmail(signInRequest.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        var jwt = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        JwtAuthenticationResponse jwtAuthenticationResponse=new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);
        return jwtAuthenticationResponse;
    }

    @Override
    public JwtAuthenticationResponse refreshToken(RefreshTokenReq refreshTokenReq) {
        String userEmail = jwtService.extractUsername(refreshTokenReq.getToken());
        Users user = usersRepo.findByEmail(userEmail).orElseThrow();

        if (jwtService.validateToken(refreshTokenReq.getToken(), user)) {
            String jwt = jwtService.generateToken(user);
            JwtAuthenticationResponse response = new JwtAuthenticationResponse();
            response.setToken(jwt);
            response.setRefreshToken(refreshTokenReq.getToken());
            return response;
        }
        return null;
    }

    @Override
    public void sendResetPasswordEmail(ResetPasswordRequest resetPasswordRequest) {

    }

    @Override
    public void updatePassword(UpdatePasswordRequest updatePasswordRequest, String email) {
        Users user = usersRepo.findByEmail(email).orElseThrow();
        if (passwordEncoder.matches(updatePasswordRequest.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
            usersRepo.save(user);
        } else {
            throw new IllegalArgumentException("Old password does not match");
        }
    }

    @Override
    public Users updateProfile(UpdateProfileRequest updateProfileRequest, String email) {
        Users user = usersRepo.findByEmail(email).orElseThrow();
        user.setFirstName(updateProfileRequest.getFirstName());
        user.setLastName(updateProfileRequest.getLastName());
        return usersRepo.save(user);
    }

    @Override
    public void verifyEmail(EmailVerificationRequest emailVerificationRequest) {

    }
}
