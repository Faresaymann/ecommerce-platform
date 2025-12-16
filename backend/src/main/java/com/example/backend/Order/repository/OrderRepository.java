package com.example.backend.Order.repository;


import com.example.backend.Order.entity.Order;
import com.example.backend.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUser(Users user, Pageable pageable);
    Optional<Order> findById(Long id);
}