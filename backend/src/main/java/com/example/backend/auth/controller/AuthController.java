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

@SuppressWarnings("ALL")

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {


    private final AuthService authService;


    /**
     * Register a new user.
     *
     * Accepts multipart/form-data so the client can optionally upload a profile picture.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email (used as unique identifier)
     * @param password the user's chosen password
     * @param role the role assigned to the user
     * @param file optional profile image file
     * @return the created registration response (including any tokens or messages)
     * @throws IOException if an error occurs while reading the uploaded file
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) Role role,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        SignUpRequest dto = new SignUpRequest(firstName, lastName, email, password, role);
        RegisterResponse response = authService.register(dto, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Verify email for a newly registered user.
     *
     * Confirms the supplied verification token and activates the user account if valid.
     *
     * @param token verification token that was sent to the user's email
     * @return a message response indicating success or failure
     */

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam("token") @NotBlank String token) {
        MessageResponse resp = authService.verifyEmail(token);
        return ResponseEntity.ok(resp);
    }


    /**
     * Authenticate a user (login).
     *
     * Validates credentials and returns login information (tokens/user info).
     *
     * @param request the sign-in request containing email and password
     * @return login response containing access and refresh tokens (or equivalent)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody SignInRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    /**
     * Refresh authentication tokens using a refresh token.
     *
     * Accepts a RefreshTokenRequest (containing the refresh token) and returns a new set of tokens.
     *
     * @param request refresh token request containing the refresh token
     * @return new login/ token response (e.g., new access token and possibly a new refresh token)
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenReq request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Log out the current user and optionally revoke a refresh token.
     *
     * This endpoint invalidates the server-side session /refresh token state for the authenticated user.
     * If a refresh token is supplied, it will be revoked; otherwise the service may revoke tokens
     * associated with the authenticated user.
     *
     * @param authentication Spring Security authentication object (used to get user email)
     * @return a message response indicating logout success
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(Authentication authentication) {
        return ResponseEntity.ok(authService.logout(authentication.getName(), null));
    }


    /**
     * Update the current user's profile.
     *
     * Allows updating the first / last name and an optional profile picture. The authenticated user's
     * identity is taken from the Security Authentication principal (email).
     *
     * @param authentication Spring Security authentication object (used to get user email)
     * @param firstName optional new first name
     * @param lastName optional new last name
     * @param file optional new profile image file
     * @return response containing updated profile details
     * @throws IOException if reading the provided file fails
     */
    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        String TokenEmail = authentication.getName(); // Extract user identity from a token

        UpdateProfileResponse response = authService.updateProfile(
                TokenEmail,
                firstName,
                lastName,
                file
        );

        return ResponseEntity.ok(response);
    }


    /**
     * Get the currently authenticated user's profile.
     *
     * Uses the authenticated principal to fetch the user's profile.
     *
     * @param authentication Spring Security authentication object (used to get user email)
     * @return the user's profile response
     */
    @GetMapping("/me")
    public ResponseEntity<GetProfileResponse> getCurrentUser(Authentication authentication) {
        GetProfileResponse resp= authService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(resp);
    }



    /**
     * Delete the currently authenticated user's account.
     *
     * Permanently deletes the user tied to the authenticated principal (email).
     *
     * @param authentication Spring Security authentication object (used to get user email)
     * @return a message response indicating deletion success or failure
     */
    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> DeleteCurrentUser(Authentication authentication){
        MessageResponse resp = authService.deleteCurrentUser(authentication.getName());
        return ResponseEntity.ok(resp);
    }



    /**
     * Request an email update for the current user.
     *
     * Triggers an email verification flow to change the user's email. The service will send
     * a verification link/token to the new email address.
     *
     * @param request contains the new email address
     * @param authentication Spring Security authentication object (used to get user email)
     * @return the original request object (or service-provided tracking information)
     */
    @PostMapping("/update-email")
    public ResponseEntity<UpdateEmailRequest> requestEmailUpdate(@Valid @RequestBody UpdateEmailRequest request, Authentication authentication) {
        UpdateEmailRequest resp = authService.requestEmailUpdate(authentication.getName(), request.getNewEmail());
        return ResponseEntity.accepted().body(resp);
    }


    /**
     * Verify updated email address using token.
     *
     * Confirms the token that was sent to the new email address and performs the actual update.
     *
     * @param token verification token
     * @return response describing the result of the email update (success/failure)
     */
    @GetMapping("/update-email/verify")
    public ResponseEntity<UpdateEmailResponse> verifyUpdatedEmail(@RequestParam("token") @NotBlank String token) {
        UpdateEmailResponse resp = authService.verifyEmailUpdate(token);
        return ResponseEntity.ok(resp);
    }


    /**
     * Update the current user's password.
     *
     * Allows authenticated users to change their password by supplying old/new credentials (as defined in UpdatePasswordRequest).
     *
     * @param request update password request containing current and new password
     * @param authentication Spring Security authentication object (used to get user email)
     * @return message response indicating whether the password update succeeded
     */
    @PutMapping("/update-password")
    public ResponseEntity<MessageResponse> updatePassword( @Valid @RequestBody UpdatePasswordRequest request, Authentication authentication) {
        MessageResponse resp = authService.updatePassword(request, authentication.getName());
        return ResponseEntity.ok(resp);
    }

    /**
     * Initiate the "forgot password" flow.
     *
     * Sends a password-reset token/link to the provided email if the account exists.
     *
     * @param body map containing "email" key
     * @return a response containing forgot password flow details
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgetPasswordResponse> forgotPassword(@RequestBody Map<String,String> body) {
        return ResponseEntity.ok(authService.forgotPassword(body.get("email")));
    }


    /**
     * Reset password using a reset token.
     *
     * Consumes a ResetPasswordRequest that includes the token and the new password.
     *
     * @param request reset password request containing token and new password
     * @return message response indicating success or failure
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    /**
     * Development/test endpoint: list all users.
     *
     * Exposes all users in the system — intended for dev/test only and should be removed or secured for production.
     *
     * @return a list of user entities
     */
    @GetMapping("/dev/users")
    public ResponseEntity<List<Users>> listUsers() {
        return ResponseEntity.ok((authService).getAllUsers());
    }




}
