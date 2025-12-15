package com.example.backend.Wishlist.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
}