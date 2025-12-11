package com.example.backend.repository;

import com.example.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<Users, Long> {
    Optional<Users>findByEmail(String email);
    //Users findByRole(Role role);
}
