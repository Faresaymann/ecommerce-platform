package com.example.backend.Auth.controller;
//All endpoints (register, login, refresh, reset password, etc.)


import com.example.backend.Auth.dto.JwtAuthenticationResponse;
import com.example.backend.Auth.dto.RegisterResponse;
import com.example.backend.Auth.dto.SignInRequest;
import com.example.backend.Auth.dto.SignUpRequest;
import com.example.backend.Auth.service.AuthService;
import com.example.backend.Auth.service.Impl.AuthServiceImpl;
import com.example.backend.entity.Users;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {


    private final AuthService authService;


    // register endpoint
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody SignUpRequest signUpRequest) {
        RegisterResponse resp = authService.register(signUpRequest);
        return ResponseEntity.ok(resp);
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

    // (dev endpoints for testing)
    @GetMapping("/dev/users")
    public ResponseEntity<List<Users>> listUsers() {
        return ResponseEntity.ok(((AuthServiceImpl)authService).getAllUsers());
    }

//    @GetMapping("/dev/token")
//    public ResponseEntity<String> getTokenForEmail(@RequestParam String email) {
//        // return latest token for email — dev only
//        return ResponseEntity.ok(((AuthServiceImpl)authService).findTokenByEmail(email));
//    }


}
