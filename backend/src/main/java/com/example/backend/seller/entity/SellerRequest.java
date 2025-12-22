package com.example.backend.seller.entity;

import com.example.backend.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "seller_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerRequest {
    @Id @GeneratedValue private Long id;

    @OneToOne(optional = false)
    private Users user;

    private String storeName;

    /**
     * URL of the uploaded document (Cloudinary secure_url).
     */
    private String documentUrl;

    /**
     * Optional message from the user explaining the application.
     */
    @Column(length = 2000)
    private String reason;

    @Enumerated(EnumType.STRING)
    private SellerRequestStatus status = SellerRequestStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    /**
     * Admin identifier (email or id) who reviewed this request.
     */
    private String reviewedBy;
}