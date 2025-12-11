package com.example.backend.Auth.controller;
//All endpoints (register, login, refresh, reset password, etc.)


import com.example.backend.Auth.dto.Requests.SignInRequest;
import com.example.backend.Auth.dto.Requests.SignUpRequest;
import com.example.backend.Auth.dto.Responses.JwtAuthenticationResponse;
import com.example.backend.Auth.dto.Responses.RegisterResponse;
import com.example.backend.Auth.dto.Responses.UpdateProfileResponse;
import com.example.backend.Auth.service.AuthService;
import com.example.backend.Auth.service.Impl.AuthServiceImpl;
import com.example.backend.entity.Users;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        SignUpRequest dto = new SignUpRequest(firstName, lastName, email, password);
        RegisterResponse resp = authService.register(dto, file);
        if ("Email already in use".equalsIgnoreCase(resp.getMessage())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        } else if (resp.getMessage().toLowerCase().contains("failed")) {
            // image upload error or email sending error — treat as 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        }

    }


    // login endpoint
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody SignInRequest request) {
        JwtAuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }



    // verify email for user Register endpoint
    @GetMapping("/verify-email")
    public ResponseEntity<RegisterResponse> verifyEmail(@RequestParam("token") String token) {
        RegisterResponse resp = authService.verifyEmail(token);
        return ResponseEntity.ok(resp);
    }


    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        String TokenEmail = authentication.getName(); // Extract user identity from token

        UpdateProfileResponse response = authService.updateProfile(
                TokenEmail,
                firstName,
                lastName,
                email,
                file
        );

        return ResponseEntity.ok(response);
    }



    // (dev endpoints for testing)
    @GetMapping("/dev/users")
    public ResponseEntity<List<Users>> listUsers() {
        return ResponseEntity.ok(((AuthServiceImpl)authService).getAllUsers());
    }

//    @GetMapping("/me")
//    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
//        String email = authentication.getName(); // gets email from JWT
//        Users user = usersRepo.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        UserResponse response = new UserResponse(user); // DTO with only needed fields
//        return ResponseEntity.ok(response);
//    }

//    @GetMapping("/dev/token")
//    public ResponseEntity<String> getTokenForEmail(@RequestParam String email) {
//        // return latest token for email — dev only
//        return ResponseEntity.ok(((AuthServiceImpl)authService).findTokenByEmail(email));
//    }


}
