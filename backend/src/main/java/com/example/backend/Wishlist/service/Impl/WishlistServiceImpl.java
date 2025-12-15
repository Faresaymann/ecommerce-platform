package com.example.backend.Wishlist.service.Impl;


import com.example.backend.Product.entity.Product;
import com.example.backend.Product.exception.ProductNotFoundException;
import com.example.backend.Product.repository.ProductRepository;
import com.example.backend.Wishlist.exception.WishlistNotFoundException;
import com.example.backend.auth.exception.UserNotFoundException;
import com.example.backend.entity.Users;
import com.example.backend.repository.UsersRepo;
import com.example.backend.Wishlist.entity.Wishlist;
import com.example.backend.Wishlist.repository.WishlistRepository;
import com.example.backend.Wishlist.dto.WishlistResponse;
import com.example.backend.Wishlist.dto.ProductDto;
import com.example.backend.Wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UsersRepo usersRepository;


    //---------------------------------------------------addToWishlist--------------------------------------------------//
    @Override
    @Transactional
    public WishlistResponse addToWishlist(String userEmail, UUID productId) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + productId));

        Wishlist wishlist = wishlistRepository.findByUser(user).orElseGet(() -> {
            Wishlist w = new Wishlist();
            w.setUser(user);
            w.setProducts(new ArrayList<>());
            return wishlistRepository.save(w);
        });

        // add if not present
        boolean exists = wishlist.getProducts().stream()
                .anyMatch(p -> p.getId().equals(product.getId()));
        if (!exists) {
            wishlist.getProducts().add(product);
            wishlistRepository.save(wishlist);
        }

        return mapToWishlistResponse(wishlist);
    }
    //---------------------------------------------------removeFromWishlist--------------------------------------------------//

    @Override
    @Transactional
    public WishlistResponse removeFromWishlist(String userEmail, UUID productId) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));

        wishlist.getProducts().removeIf(p -> p.getId().equals(productId));
        wishlistRepository.save(wishlist);

        return mapToWishlistResponse(wishlist);
    }
    //---------------------------------------------------getWishlist--------------------------------------------------//

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Wishlist wishlist = wishlistRepository.findByUser(user).orElseGet(() -> {
            Wishlist w = new Wishlist();
            w.setUser(user);
            w.setProducts(new ArrayList<>());
            return w;
        });

        return mapToWishlistResponse(wishlist);
    }
    //---------------------------------------------------clearWishlist--------------------------------------------------//

    @Override
    public WishlistResponse clearWishlist(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found"));

        wishlist.getProducts().clear();
        wishlistRepository.save(wishlist);

        return mapToWishlistResponse(wishlist);
    }
    //---------------------------------------------------mapping helpers--------------------------------------------------//

    // --- mapToWishlistResponse --- //
    private WishlistResponse mapToWishlistResponse(Wishlist w) {

        List<ProductDto> products = (w.getProducts() == null ? Collections.emptyList() : w.getProducts())
                .stream()
                .map(o -> {
                    Product p = (Product) o;
                    return ProductDto.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .description(p.getDescription())
                            .price(p.getPrice() != null ? p.getPrice().doubleValue() : 0.0)
                            .imageUrl(p.getImageUrl())
                            .build();
                })
                .collect(Collectors.toList());

        return WishlistResponse.builder()
                .wishlistId(w.getId())
                .products(products)
                .build();
    }

}
