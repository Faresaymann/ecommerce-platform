package com.example.backend.Cart.Controller;

import com.example.backend.Cart.dto.CartItemRequest;
import com.example.backend.Cart.dto.CartResponse;
import com.example.backend.Cart.service.CartService;
import com.example.backend.auth.dto.Responses.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Validated
public class CartController {
    private final CartService cartService;


    /**
     * Add product to cart (creates cart if missing).
     * Body: { "productId": "<uuid>", "quantity": 1 }
     */
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> addToCart(
            Authentication authentication,
            @RequestBody @Valid CartItemRequest request) {

        String userEmail = authentication.getName();
        CartResponse resp = cartService.addToCart(userEmail, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(resp);
    }

    /**
     * Update quantity of an existing cart item. If quantity <= 0, item is removed.
     * Body: { "productId": "<uuid>", "quantity": 2 }
     */
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> updateQuantity(
            Authentication authentication,
            @RequestBody @Valid CartItemRequest request) {

        String userEmail = authentication.getName();
        CartResponse resp = cartService.updateQuantity(userEmail, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(resp);
    }

    /**
     * Remove a product from the cart by productId (path).
     */
    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> removeFromCart(
            Authentication authentication,
            @PathVariable UUID productId) {

        String userEmail = authentication.getName();
        CartResponse resp = cartService.removeFromCart(userEmail, productId);
        return ResponseEntity.ok(resp);
    }

    /**
     * Get the current user's cart.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        String userEmail = authentication.getName();
        CartResponse resp = cartService.getCart(userEmail);
        return ResponseEntity.ok(resp);
    }

    /**
     * Clear user's cart (delete all items). Returns 204 No Content.
     */
    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> clearCart(Authentication authentication) {
        String userEmail = authentication.getName();
        cartService.clearCart(userEmail);
        return ResponseEntity.ok(new MessageResponse("Cart cleared."));
    }


}
