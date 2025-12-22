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

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Validated
public class CartController {
    private final CartService cartService;


    /**
     * Add a product to the user's cart.
     *
     * If the user has no existing cart, a new cart is created automatically.
     *
     * @param authentication Spring Security authentication object (used to get user email)
     * @param request the cart item request containing productId and quantity
     * @return the updated {@link CartResponse} including all current items
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
     * Update the quantity of an existing cart item.
     *
     * If the quantity is set to 0 or less, the item is removed from the cart.
     *
     * @param authentication Spring Security authentication object (used to get user email)
     * @param request the cart item request containing productId and the new quantity
     * @return the updated {@link CartResponse} including all current items
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
     * Update the quantity of an existing cart item.
     *
     * If the quantity is set to 0 or less, the item is removed from the cart.
     *
     * @param authentication Spring Security authentication object (used to get user email)
     * @param productId the cart item request containing productId and the new quantity
     * @return the updated {@link CartResponse} including all current items
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
     * Retrieve the current user's cart.
     *
     * @param authentication Spring Security authentication object (used to get user email)
     * @return the {@link CartResponse} including all items currently in the cart
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        String userEmail = authentication.getName();
        CartResponse resp = cartService.getCart(userEmail);
        return ResponseEntity.ok(resp);
    }

    /**
     * Clear all items from the current user's cart.
     *
     * @param authentication Spring Security authentication object (used to get user email)
     * @return a {@link MessageResponse} confirming that the cart has been cleared
     */
    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> clearCart(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(cartService.clearCart(userEmail));
    }


}
