package com.example.backend.Wishlist.Controller;

import com.example.backend.Wishlist.dto.WishlistResponse;
import com.example.backend.Wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;



    /**
     * Add a product to the authenticated user's wishlist.
     *
     * @param auth current authenticated user
     * @param productId UUID of the product to add
     * @return the updated wishlist
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<WishlistResponse> addToWishlist(Authentication auth, @PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(auth.getName(), productId));
    }


    /**
     * Remove a product from the authenticated user's wishlist.
     *
     * @param auth current authenticated user
     * @param productId UUID of the product to remove
     * @return the updated wishlist
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<WishlistResponse> removeFromWishlist(Authentication auth, @PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(auth.getName(), productId));
    }



    /**
     * Retrieve the authenticated user's wishlist.
     *
     * @param auth current authenticated user
     * @return the wishlist
     */    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(Authentication auth) {
        return ResponseEntity.ok(wishlistService.getWishlist(auth.getName()));
    }


    /**
     * Clear all items from the authenticated user's wishlist.
     *
     * @param authentication current authenticated user
     * @return the empty wishlist
     */
    @DeleteMapping("/clear")
    public ResponseEntity<WishlistResponse> clearWishlist(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(wishlistService.clearWishlist(email));
    }
}
