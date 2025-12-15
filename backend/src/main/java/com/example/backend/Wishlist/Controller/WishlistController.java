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

    @PostMapping("/add/{productId}")
    public ResponseEntity<WishlistResponse> addToWishlist(Authentication auth, @PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(auth.getName(), productId));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<WishlistResponse> removeFromWishlist(Authentication auth, @PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(auth.getName(), productId));
    }

    // get user's wishlist'
    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(Authentication auth) {
        return ResponseEntity.ok(wishlistService.getWishlist(auth.getName()));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<WishlistResponse> clearWishlist(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(wishlistService.clearWishlist(email));
    }
}
