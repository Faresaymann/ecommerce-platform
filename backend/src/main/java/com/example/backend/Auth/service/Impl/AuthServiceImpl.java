package com.example.backend.Auth.service.Impl;

import com.example.backend.Auth.dto.*;
import com.example.backend.Auth.service.AuthService;
import com.example.backend.entity.Role;
import com.example.backend.entity.Users;
import com.example.backend.entity.VerificationToken;
import com.example.backend.repository.UsersRepo;
import com.example.backend.repository.VerificationTokenRepo;
import com.example.backend.util.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepo verificationTokenRepo;
    private final AuthenticationManager authenticationManager;
    private final JWTserviceImpl jwtService;



//register user without Email verification
//    @Override
//    public Users register(SignUpRequest signUpRequest) {
//        Users user = new Users();
//        user.setFirstName(signUpRequest.getFirstName());
//        user.setLastName(signUpRequest.getLastName());
//        user.setEmail(signUpRequest.getEmail());
//        user.setRole(Role.ROLE_USER);
//        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
//        return usersRepo.save(user);
//    }


//register user with Email verification
@Override
@Transactional
public RegisterResponse register(SignUpRequest signUpRequest) {
    // check if email already exists
    if (usersRepo.findByEmail(signUpRequest.getEmail()).isPresent()) {
        return new RegisterResponse("Email already in use");
    }

    // create new user
    Users user = Users.builder()
            .firstName(signUpRequest.getFirstName())
            .lastName(signUpRequest.getLastName())
            .email(signUpRequest.getEmail())
            .role(Role.ROLE_USER)
            .password(passwordEncoder.encode(signUpRequest.getPassword()))
            .enabled(false) // inactive until verified
            .createdAt(LocalDateTime.now())
            .build();
    usersRepo.save(user);


    // generate and store token
    String token = UUID.randomUUID().toString();

    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(token);
    verificationToken.setUser(user);
    verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
    verificationTokenRepo.save(verificationToken);


    // send verification email ( change the URL depending on your deployment(frontend(3000) or backend(8080)) )
    String verificationLink = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;
    String body = "Hello " + user.getFirstName() + ",\n\n" +
            "Click the link to verify your account:\n" + verificationLink +
            "\n\nIf you did not register, ignore this email.";
    emailService.sendEmail(user.getEmail(), "Verify your account", body);

    return new RegisterResponse("User registered. Please check your email for verification." ,token);
}


//-----------------------------------------------------------------------------------------------------//

    // verify email using token
    @Override
    @Transactional
    public RegisterResponse verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (verificationToken.isUsed() || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new RegisterResponse("Token invalid or expired");
        }

        Users user = verificationToken.getUser();
        user.setEmailVerified(true);                 //  user.setEnabled(true)
        user.setEnabled(true);
        usersRepo.save(user);
        verificationToken.setUsed(true);
        verificationTokenRepo.save(verificationToken);

        return new RegisterResponse("Email verified successfully!");
    }


    //-----------------------------------------------------------------------------------------------------//
    // login user and generate JWT
    @Override
    public JwtAuthenticationResponse login(SignInRequest signInRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword())
        );

        Users user = usersRepo.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        // check emailVerified/enabled:
        if (!user.isEnabled()) {
            throw new IllegalStateException("Email not verified");
        }

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        JwtAuthenticationResponse resp = new JwtAuthenticationResponse();
        resp.setToken(token);
        resp.setRefreshToken(refreshToken);
        return resp;
    }

    //-----------------------------------------------------------------------------------------------------//
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
    //-----------------------------------------------------------------------------------------------------//
    @Override
    public void sendResetPasswordEmail(ResetPasswordRequest resetPasswordRequest) {

    }
    //-----------------------------------------------------------------------------------------------------//
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
    //-----------------------------------------------------------------------------------------------------//
    @Override
    public Users updateProfile(UpdateProfileRequest updateProfileRequest, String email) {
        Users user = usersRepo.findByEmail(email).orElseThrow();
        user.setFirstName(updateProfileRequest.getFirstName());
        user.setLastName(updateProfileRequest.getLastName());
        return usersRepo.save(user);
    }
//-----------------------------------------------------------------------------------------------------//

    public @Nullable List<Users> getAllUsers() {
        return usersRepo.findAll();
    }
//-----------------------------------------------------------------------------------------------------//
//    public @Nullable Users findTokenByEmail(String email) {
//        return usersRepo.findByEmail(email).orElseThrow();
//    }
}
