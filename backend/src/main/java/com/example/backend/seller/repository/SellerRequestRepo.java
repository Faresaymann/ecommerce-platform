package com.example.backend.seller.repository;

import com.example.backend.entity.Users;
import com.example.backend.seller.entity.SellerRequest;
import com.example.backend.seller.entity.SellerRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

@SuppressWarnings("ALL")
public interface SellerRequestRepo extends JpaRepository<SellerRequest, Long> {
    Optional<SellerRequest> findByUser(Users user);

    List<SellerRequest> findAllByStatus(SellerRequestStatus sellerRequestStatus);
}