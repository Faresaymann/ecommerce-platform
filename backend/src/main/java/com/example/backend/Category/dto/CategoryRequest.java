package com.example.backend.Category.dto;


import jakarta.validation.constraints.*;
import lombok.*;

import javax.net.ssl.SSLSession;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name must be at most 50 characters")
    private String name;

    @Size(max = 250, message = "Description must be at most 250 characters")
    private String description;

    private Long parentId;

}
