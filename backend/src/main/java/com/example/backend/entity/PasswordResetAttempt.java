package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "password_reset_attempts")
public class PasswordResetAttempt {

    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private int attempts;
    private LocalDateTime lastAttempt;
}
