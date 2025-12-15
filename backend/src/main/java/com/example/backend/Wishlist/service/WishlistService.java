package com.example.backend.Wishlist.service;
import com.example.backend.Wishlist.dto.WishlistResponse;

import java.util.UUID;

public interface WishlistService {
    WishlistResponse addToWishlist(String userEmail, UUID productId);
    WishlistResponse removeFromWishlist(String userEmail, UUID productId);
    WishlistResponse getWishlist(String userEmail);
    WishlistResponse clearWishlist(String userEmail);
}
