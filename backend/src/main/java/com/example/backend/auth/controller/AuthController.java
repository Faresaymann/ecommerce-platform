package com.example.backend.auth.controller;
//All endpoints (register, login, refresh, reset password, etc.)


import com.example.backend.auth.dto.Requests.*;
import com.example.backend.auth.dto.Responses.*;
import com.example.backend.auth.service.AuthService;
import com.example.backend.entity.Role;
import com.example.backend.entity.Users;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {


    private final AuthService authService;


    // register endpoint
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Role role,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        SignUpRequest dto = new SignUpRequest(firstName, lastName, email, password, role);
        RegisterResponse response = authService.register(dto, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // verify email for user Register endpoint
    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam("token") @NotBlank String token) {
        MessageResponse resp = authService.verifyEmail(token);
        return ResponseEntity.ok(resp);
    }


    // login endpoint
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody SignInRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }



    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        String TokenEmail = authentication.getName(); // Extract user identity from token

        UpdateProfileResponse response = authService.updateProfile(
                TokenEmail,
                firstName,
                lastName,
                file
        );

        return ResponseEntity.ok(response);
    }

    //get current user profile
    @GetMapping("/me")
    public ResponseEntity<GetProfileResponse> getCurrentUser(Authentication authentication) {
        GetProfileResponse resp= authService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(resp);
    }



    //delete current user profile
    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> DeleteCurrentUser(Authentication authentication){
        MessageResponse resp = authService.DeleteCurrentUser(authentication.getName());
        return ResponseEntity.ok(resp);
    }


    // update email
    @PostMapping("/update-email")
    public ResponseEntity<UpdateEmailRequest> requestEmailUpdate(@Valid @RequestBody UpdateEmailRequest request, Authentication authentication) {
        UpdateEmailRequest resp = authService.requestEmailUpdate(authentication.getName(), request.getNewEmail());
        return ResponseEntity.accepted().body(resp);
    }

    @GetMapping("/update-email/verify")
    public ResponseEntity<UpdateEmailResponse> verifyUpdatedEmail(@RequestParam("token") @NotBlank String token) {
        UpdateEmailResponse resp = authService.verifyEmailUpdate(token);
        return ResponseEntity.ok(resp);
    }


    // update password
    @PutMapping("/update-password")
    public ResponseEntity<MessageResponse> updatePassword( @Valid @RequestBody UpdatePasswordRequest request, Authentication authentication) {
        MessageResponse resp = authService.updatePassword(request, authentication.getName());
        return ResponseEntity.ok(resp);
    }

    // forget password
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgetPasswordResponse> forgotPassword(@RequestBody Map<String,String> body) {
        return ResponseEntity.ok(authService.forgotPassword(body.get("email")));
    }

    // reset password
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    // (dev endpoints for testing)
    @GetMapping("/dev/users")
    public ResponseEntity<List<Users>> listUsers() {
        return ResponseEntity.ok((authService).getAllUsers());
    }




}
