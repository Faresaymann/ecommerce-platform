package com.example.backend.Cart.service.Impl;

import com.example.backend.Cart.dto.CartItemResponse;
import com.example.backend.Cart.dto.CartResponse;
import com.example.backend.Cart.entity.Cart;
import com.example.backend.Cart.entity.CartItem;
import com.example.backend.Cart.repository.CartItemRepository;
import com.example.backend.Cart.repository.CartRepository;
import com.example.backend.Cart.service.CartService;
import com.example.backend.Product.entity.Product;
import com.example.backend.Product.exception.*;
import com.example.backend.Product.repository.ProductRepository;
import com.example.backend.auth.dto.Responses.MessageResponse;
import com.example.backend.auth.exception.UserNotFoundException;
import com.example.backend.entity.Users;
import com.example.backend.repository.UsersRepo;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UsersRepo usersRepository;

    //---------------------------------------------------addToCart--------------------------------------------------//
    @Override
    @Transactional
    public CartResponse addToCart(String userEmail, UUID productId, Integer quantity) {
        if (quantity == null || quantity <= 0) quantity = 1;

        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + productId));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductOutOfStockException("Product is not active");
        }
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new ProductOutOfStockException("Not enough stock for product " + product.getName());
        }

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setItems(new ArrayList<>());
            return cartRepository.save(c);
        });

        // find existing item
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        // update quantity
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            if (product.getStock() < newQty) {
                throw new ProductOutOfStockException("Not enough stock to increase quantity to " + newQty);
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
            cartItemRepository.save(item);
            cartRepository.save(cart);
        }
        return mapToCartResponse(cart);

    }
    //---------------------------------------------------updateQuantity--------------------------------------------------//


    @Override
    @Transactional
    public CartResponse updateQuantity(String userEmail, UUID productId, Integer quantity) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found")); // or custom exception

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not found in cart: " + productId));

        // Update quantity (if the new quantity <=0 -> remove)
        if (quantity == null || quantity <= 0) {
            // remove
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
            return mapToCartResponse(cart);

        }

        Product product = item.getProduct();
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new ProductOutOfStockException("Not enough stock for product " + product.getName());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return mapToCartResponse(cart);

    }
    //---------------------------------------------------removeFromCart--------------------------------------------------//
    @Override
    @Transactional
    public CartResponse removeFromCart(String userEmail, UUID productId) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
        }

        return mapToCartResponse(cart);

    }
    //---------------------------------------------------getCart--------------------------------------------------//
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setItems(new ArrayList<>());
            return c;
        });

        return mapToCartResponse(cart);

    }
    //---------------------------------------------------clearCart--------------------------------------------------//
    @Override
    @Transactional
    public MessageResponse clearCart(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        cartRepository.findByUser(user).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);
        });
        return new MessageResponse();
    }

    //---------------------------------------------------mapping helpers--------------------------------------------------//

    // --- mapToCartResponse --- //
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = (cart.getItems() == null ? Collections.emptyList() : cart.getItems()).stream()
                .map(o -> {
                    CartItem i = (CartItem) o;
                    return CartItemResponse.builder()
                            .productId(i.getProduct().getId())
                            .productName(i.getProduct().getName())
                            .price(i.getProduct().getPrice())
                            .quantity(i.getQuantity())
                            .build();
                }).collect(Collectors.toList());


        BigDecimal total = (cart.getItems() == null ? Collections.emptyList() : cart.getItems()).stream()
                .map(o -> {
                    CartItem i = (CartItem) o;
                    BigDecimal price = i.getProduct().getPrice() == null ? BigDecimal.ZERO : i.getProduct().getPrice();
                    return price.multiply(BigDecimal.valueOf(i.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalPrice(total.doubleValue())
                .build();
    }
}
