package com.example.backend.exception;

import com.example.backend.auth.dto.Responses.MessageResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyUsedException(EmailAlreadyUsedException ex) {
        Map<String, Object> body = Map.of(
        "status", 409,
        "timestamp", LocalDateTime.now(),
        "message", ex.getMessage(),
        "error", "Conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTokenException(InvalidTokenException ex) {
        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Bad Request"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwt(HttpServletRequest req) {
        Map<String,Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", "Token expired",
                "path", req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleBadCreds(InvalidCredentialsException ex, HttpServletRequest req) {
        Map<String,Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", ex.getMessage(),
                "path", req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<?> handleInvalidOtp(InvalidOtpException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<?> handleTooManyRequests(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new MessageResponse(ex.getMessage()));
    }


    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<?> handleAccountNotVerified(AccountNotVerifiedException ex) {
        // Maybe 403 or 401 depending on your semantics; using 403 here
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // fallback - catches everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        ex.printStackTrace(); // optional: server logs
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong: " + ex.getMessage());
    }

    private ResponseEntity<?> build(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
        return ResponseEntity.status(status).body(body);
    }
}
