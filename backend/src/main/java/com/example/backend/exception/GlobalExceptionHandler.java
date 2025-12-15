package com.example.backend.exception;

import com.example.backend.Category.exception.*;
import com.example.backend.Product.exception.*;
import com.example.backend.auth.dto.Responses.MessageResponse;
import com.example.backend.auth.exception.AccountNotVerifiedException;
import com.example.backend.auth.exception.EmailAlreadyUsedException;
import com.example.backend.auth.exception.InvalidCredentialsException;
import com.example.backend.auth.exception.InvalidOtpException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // ----------------- Product module exceptions ----------------- //

    // check if the product exists when Searching
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "status", 404,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Not Found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // check if the product Already exists
    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleProductAlreadyExists(ProductAlreadyExistsException ex) {
        Map<String, Object> body = Map.of(
                "status", 409,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // check if product cardinalities are valid
    @ExceptionHandler(InvalidProductException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidProduct(InvalidProductException ex) {
        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Bad Request"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // check if the product is active
    @ExceptionHandler(ProductInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleProductInactive(ProductInactiveException ex) {
        Map<String, Object> body = Map.of(
                "status", 403,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Forbidden"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // check if a product is in stock
    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<Map<String, Object>> handleProductOutOfStock(ProductOutOfStockException ex) {
        Map<String, Object> body = Map.of(
                "status", 409,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Check if the user owns the product
    @ExceptionHandler(ProductOwnershipException.class)
    public ResponseEntity<Map<String,Object>> handleOwnership(ProductOwnershipException ex) {
        Map<String, Object> body = Map.of(
                "status", 403,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Forbidden"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }


    // ----------------- Category module exceptions ----------------- //

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "status", 404,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Not Found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({CategoryAlreadyExistsException.class, InvalidCategoryException.class, CategoryUpdateException.class, CategoryDeletionException.class})
    public ResponseEntity<Map<String, Object>> handleCategoryBusinessExceptions(RuntimeException ex) {
        Map<String, Object> body = Map.of(
                "status", 409,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }


    // ----------------- Auth module exceptions ----------------- //

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
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // fallback - catches everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        ex.printStackTrace(); // server logs
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
