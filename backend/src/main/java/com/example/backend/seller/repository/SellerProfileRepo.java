package com.example.backend.seller.repository;

import com.example.backend.entity.Users;
import com.example.backend.seller.entity.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@SuppressWarnings("ALL")
public interface SellerProfileRepo extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUser(Users user);
}