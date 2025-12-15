package com.example.backend.Product.dto;

import lombok.*;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private String description;
    private Double price;
    private Long categoryId;
    private Integer stock;

}
