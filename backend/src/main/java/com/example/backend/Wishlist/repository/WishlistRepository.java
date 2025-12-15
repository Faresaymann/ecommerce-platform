package com.example.backend.Wishlist.repository;

import com.example.backend.Wishlist.entity.Wishlist;
import com.example.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    Optional<Wishlist> findByUser(Users user);
}