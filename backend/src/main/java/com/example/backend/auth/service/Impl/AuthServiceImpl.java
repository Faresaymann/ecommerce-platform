package com.example.backend.auth.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.backend.auth.dto.Requests.*;
import com.example.backend.auth.dto.Responses.*;
import com.example.backend.auth.exception.*;
import com.example.backend.entity.*;
import com.example.backend.exception.*;
import com.example.backend.auth.service.AuthService;
import com.example.backend.repository.*;
import com.example.backend.util.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;


/**
 * Authentication and user account service implementation.
 *
 * Handles all authentication-related business logic including
 * - User registration and email verification
 * - Login and JWT token generation
 * - Refresh token flow (issuing new access tokens)
 * - Logout (stateless JWT)
 * - Profile management
 * - Password management (update, forgot, reset)
 * - Email update with verification
 *
 * This service uses stateless JWT authentication where:
 * - Access tokens are short-lived
 * - Refresh tokens are long-lived and used only to get new access tokens
 */

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepo verificationTokenRepo;
    private final AuthenticationManager authenticationManager;
    private final JWTserviceImpl jwtService;
    private final Cloudinary cloudinary;
    private final OTPRepository otpRepository;
    private final PasswordResetAttemptRepository attemptRepo;





    /**
     * Register a new user account.
     *
     * Creates a new user with the provided signup details, uploads an optional
     * profile image to Cloudinary, generates an email verification token,
     * and sends a verification email to the user.
     *
     * The user account remains disabled until email verification is completed.
     *
     * @param signUpRequest the signup request containing user details
     * @param file optional profile image file
     * @return a response indicating successful registration
     * @throws IOException if image upload fails
     */
    @Override
    @Transactional
    public RegisterResponse register(SignUpRequest signUpRequest , MultipartFile file) throws IOException {
    // check if email already exists
        if (usersRepo.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException("The new email is already in use.");
        }
        // Determine a role: default to USER if null (because if I want to create new roles for devs (usage only ex. adding category(admin) , adding product(seller))
        Role userRole = signUpRequest.getRole() != null ? signUpRequest.getRole() : Role.ROLE_USER;
    // create new user
    Users user = Users.builder()
            .firstName(signUpRequest.getFirstName())
            .lastName(signUpRequest.getLastName())
            .email(signUpRequest.getEmail())
            .role(userRole)
            .password(passwordEncoder.encode(signUpRequest.getPassword()))
            .enabled(false) // inactive until verified
            .createdAt(LocalDateTime.now())
            .build();

            // Upload image to Cloudinary
            if (file != null && !file.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "Customers",// folder name
                                "public_id", signUpRequest.getEmail(),//file name
                                "overwrite", true,
                                "resource_type", "image"
                        )
                );
                String imageUrl = (String) uploadResult.get("secure_url");
                user.setProfileImageUrl(imageUrl);
            }

    //save user to DB
    usersRepo.save(user);


    // generate and store token
    String token = UUID.randomUUID().toString();

    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(token);
    verificationToken.setUser(user);
    verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
    verificationTokenRepo.save(verificationToken);


    // send verification email (change the URL depending on your deployment(frontend(3000) or backend(8080)) )
    String verificationLink = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;
    String body = "Hello " + user.getFirstName() + ",\n\n" +
            "Click the link to verify your account:\n" + verificationLink +
            "\n\nIf you did not register, ignore this email.";
    emailService.sendEmail(user.getEmail(), "Verify your account", body);

    return new RegisterResponse("User registered. Please check your email for verification." ,token);
}




    /**
     * Verify a user's email address.
     *
     * Validates the verification token sent to the user's email.
     * If valid and not expired, the user account is activated.
     *
     * @param token the email verification token
     * @return a message indicating verification result
     */
    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (verificationToken.isUsed() || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new MessageResponse("Token invalid or expired");
        }

        Users user = verificationToken.getUser();
        user.setEmailVerified(true);                 //  user.setEnabled(true)
        user.setEnabled(true);
        usersRepo.save(user);
        verificationToken.setUsed(true);
        verificationTokenRepo.save(verificationToken);

        return new MessageResponse("Email verified successfully!");
    }




    /**
     * Authenticate a user and generate JWT tokens.
     *
     * Validates user credentials using Spring Security authentication.
     * If successful, generates an access token and a refresh token.
     *
     * @param signInRequest login request containing email and password
     * @return login response containing tokens and user details
     * @throws InvalidCredentialsException if authentication fails
     * @throws AccountNotVerifiedException if email is not verified
     */
    @Override
    public LoginResponse login(SignInRequest signInRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword())
            );
            // error handling
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        Users user = usersRepo.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Please verify your email first");
        }


        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse (
            "Login successful",
                token, refreshToken,
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getProfileImageUrl(),
            user.getRole()
        );
    }


    /**
     * Refresh an expired access token.
     *
     * Validates the provided refresh token and issues a new access token
     * while reusing the same refresh token.
     *
     * This method does not require database-backed refresh tokens
     * and follows a stateless JWT approach.
     *
     * @param refreshTokenReq request containing the refresh token
     * @return login response with a new access token
     * @throws InvalidTokenException if the refresh token is invalid or expired
     */

    @Override
    public LoginResponse refreshToken(RefreshTokenReq refreshTokenReq) {
        String email;

        try {
            email = jwtService.extractUsername(refreshTokenReq.getToken());
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Validate refresh token
        if (!jwtService.validateToken(refreshTokenReq.getToken(), user)) {
            throw new InvalidTokenException("Refresh token expired or invalid");
        }

        // Generate a NEW access token
        String newAccessToken = jwtService.generateToken(user);

        return new LoginResponse(
                "Token refreshed successfully",
                newAccessToken,
                refreshTokenReq.getToken(), // reuse same refresh token
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfileImageUrl(),
                user.getRole()
        );
    }



    /**
     * Logout the current user.
     *
     * In a stateless JWT setup, logout is handled client-side by
     * deleting the access and refresh tokens.
     * No server-side action is required.
     *
     * @param email authenticated user's email
     * @param refreshToken optional refresh token (currently unused)
     * @return message confirming logout
     */
    @Override
    public MessageResponse logout(String email, String refreshToken) {
        // Stateless JWT → nothing to do server-side
        return new MessageResponse("Logged out successfully");
    }



    /**
     * Update the authenticated user's password.
     *
     * Verifies the old password before updating to a new encrypted password.
     *
     * @param request request containing old and new passwords
     * @param currentUserEmail authenticated user's email
     * @return message indicating success
     */
    @Override
    @Transactional
    public MessageResponse updatePassword(UpdatePasswordRequest request, String currentUserEmail) {

        Users user = usersRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepo.save(user);

        return new MessageResponse("Password updated successfully");
    }



    /**
     * Update the authenticated user's profile.
     *
     * Allows updating first name, last name, and profile image.
     * Profile images are uploaded to Cloudinary.
     *
     * @param userEmail authenticated user's email
     * @param firstName optional new first name
     * @param lastName optional new last name
     * @param file optional new profile image
     * @return response containing updated profile data
     * @throws IOException if image upload fails
     */
    @Override
    public UpdateProfileResponse updateProfile(String userEmail, String firstName, String lastName, MultipartFile file) throws IOException {

        Users user = usersRepo.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update special fields only if present
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);

        // Upload a new profile photo
        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "Customers",// folder name
                            "public_id", userEmail, //file name
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            user.setProfileImageUrl(imageUrl);
        }

        usersRepo.save(user);

        return new UpdateProfileResponse(
                "Profile updated successfully",
                user.getFirstName(),
                user.getLastName(),
                user.getProfileImageUrl()
        );
    }





    /**
     * Retrieve the authenticated user's profile.
     *
     * @param email authenticated user's email
     * @return profile response containing user details
     */
    public GetProfileResponse getUserProfile(String email) {
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return new GetProfileResponse(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfileImageUrl()
        );
    }



    /**
     * Permanently delete the authenticated user's account.
     *
     * Removes the user and any associated verification tokens.
     *
     * @param email authenticated user's email
     * @return message indicating successful deletion
     */
    @Transactional
    public MessageResponse deleteCurrentUser(String email) {
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        verificationTokenRepo.deleteByUser(user);
        usersRepo.delete(user);
        return new MessageResponse(
          " User Deleted Successfully :( "
        );
    }



    /**
     * Request an email address update.
     *
     * Sends a verification email to the new email address.
     * The change is only applied after verification.
     *
     * @param currentEmail user's current email
     * @param newEmail requested new email
     * @return response indicating verification email was sent
     */
    @Transactional
    public UpdateEmailRequest requestEmailUpdate(String currentEmail, String newEmail) {
        Users user = usersRepo.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if a new email is already in use
        if (usersRepo.findByEmail(newEmail).isPresent()) {
            throw new EmailAlreadyUsedException("The new email is already in use.");
        }
        // Generate verification token
        VerificationToken token = verificationTokenRepo.findByUser(user)
                .orElse(new VerificationToken());
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusHours(24));
        token.setUsed(false);
        token.setNewEmail(newEmail);
        verificationTokenRepo.save(token);

        // Send email with token link
        String verificationLink = "http://localhost:8080/api/v1/auth/update-email/verify?token=" + token;
        String body = "Hello " + user.getFirstName() + ",\n\n" +
                "Click the link to verify your account:\n" + verificationLink +
                "\n\nIf you did not try to change your email , ignore this email.";
        emailService.sendEmail(newEmail, "Verify your email", "Click to verify: " + body);

        return new UpdateEmailRequest(
                "Verification email sent. Please check your inbox to confirm your new email.",
                newEmail,
                token.getToken()
        );

    }





    /**
     * Verify and apply a pending email update.
     *
     * Validates the verification token and updates the user's email address.
     *
     * @param tokenStr email update verification token
     * @return response confirming email update
     */
    @Transactional
    public UpdateEmailResponse verifyEmailUpdate(String tokenStr) {
        VerificationToken token = verificationTokenRepo.findByToken(tokenStr)
                .orElseThrow(() -> new InvalidTokenException("The verification token is invalid or expired."));

        if (token.isUsed()) {
            throw new InvalidTokenException("This token has already been used.");
        }
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("This token has expired.");
        }

        Users user = token.getUser();
        user.setEmail(token.getNewEmail()); // new email from request saved in token
        token.setUsed(true);
        usersRepo.save(user);
        verificationTokenRepo.save(token);
        return new UpdateEmailResponse("Email updated successfully" , user.getEmail());
    }




    /**
     * Retrieve all users.
     *
     * Intended for development or administrative use.
     *
     * @return list of all users
     */
    public @Nullable List<Users> getAllUsers() {
        return usersRepo.findAll();
    }





    /**
     * Initiate the password reset process.
     *
     * Generates a one-time password (OTP), stores it temporarily,
     * and sends it to the user's email.
     * <p>
     * Rate limits reset attempts to prevent abuse.
     *
     * @param currentEmail user's email
     * @return response indicating OTP was sent
     */
    @Transactional
    @Override
    public ForgetPasswordResponse forgotPassword(String currentEmail) {
        Users user = usersRepo.findByEmail(currentEmail)
                .orElseThrow(() -> new InvalidCredentialsException("User not found with email: " + currentEmail));

        PasswordResetAttempt attempt =
                attemptRepo.findByEmail(currentEmail)
                        .orElse(new PasswordResetAttempt());

        if (attempt.getAttempts() >= 3 &&
                attempt.getLastAttempt().isAfter(LocalDateTime.now().minusHours(1))) {
            throw new TooManyRequestsException("Too many reset attempts. Try again later.");
        }

        // Update attempts
        attempt.setEmail(currentEmail);
        attempt.setAttempts(attempt.getAttempts() + 1);
        attempt.setLastAttempt(LocalDateTime.now());
        attemptRepo.save(attempt);


        // generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(1_000_000));

        // Optionally clean previous OTPs
        otpRepository.deleteByUser(user);

        // Save OTP in the database
        OTP otpEntity = new OTP();
        otpEntity.setUser(user);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // OTP expiry time (15 minutes)
        otpRepository.save(otpEntity);

        // send email (do NOT include OTP in response)
        String body = "Hello " + user.getFirstName() + ",\n\n" +
                "Your password reset code (OTP) is: " + otp + "\n\n" +
                "This code expires in 15 minutes.";
        emailService.sendEmail(user.getEmail(), "Password Reset OTP", body);

        return new ForgetPasswordResponse("OTP sent successfully. Please check your inbox." , otp);
    }




    /**
     * Reset the user password using OTP verification.
     *
     * Validates the OTP and updates the user's password if valid.
     * OTP is deleted after successful use.
     *
     * @param request reset a password request containing OTP and a new password
     * @return message indicating password reset success
     */
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        Users user = usersRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        OTP otpEntity = otpRepository.findByUserAndOtp(user, request.getOtp())
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));

        if (otpEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpEntity);
            throw new InvalidOtpException("OTP expired");
        }

        // update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepo.save(user);

        // delete OTP after successful use
        otpRepository.delete(otpEntity);

        return new MessageResponse("Password reset successfully");
    }
}
