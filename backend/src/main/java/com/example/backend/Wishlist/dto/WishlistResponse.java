package com.example.backend.Wishlist.dto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistResponse {
    private UUID wishlistId;
    private List<ProductDto> products;
}