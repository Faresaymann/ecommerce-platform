package com.example.backend.Product.controller;

import com.example.backend.Product.dto.ProductRequest;
import com.example.backend.Product.dto.ProductResponse;
import com.example.backend.Product.service.ProductService;
import com.example.backend.auth.dto.Responses.MessageResponse;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;


    /**
     * Create a new product.
     *
     * Accepts multipart/form-data to optionally include a product image.
     * Only accessible by users with SELLER role.
     *
     * @param authentication the current authenticated user (used to get seller email)
     * @param name the product name
     * @param description the product description
     * @param price the product price
     * @param categoryId the ID of the category the product belongs to
     * @param stock the available stock quantity
     * @param file optional product image
     * @return the created ProductResponse with all product details
     * @throws IOException if there is an error reading the uploaded file
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Double price,
            @RequestParam Long categoryId,
            @RequestParam Integer stock,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ProductRequest request = new ProductRequest(name, description, price, categoryId, stock);
        String sellerEmail = authentication.getName();
        ProductResponse resp = productService.createProduct(request, file, sellerEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }


    /**
     * Get all products.
     *
     * Public endpoint that lists all products.
     *
     * @return list of ProductResponse objects
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Get a product by its ID.
     *
     * @param id the UUID of the product
     * @return ProductResponse for the requested product
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }


    /**
     * Update an existing product.
     *
     * Only accessible by the product owner (SELLER) or ADMIN.
     * Accepts multipart/form-data to optionally update product image.
     *
     * @param id the UUID of the product
     * @param authentication the current authenticated user (used to verify owner)
     * @param name optional new product name
     * @param description optional new product description
     * @param price optional new price
     * @param categoryId optional new category ID
     * @param stock optional new stock quantity
     * @param file optional new product image
     * @return updated ProductResponse
     * @throws IOException if there is an error reading the uploaded file
     */    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double price,           // optional now
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer stock,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ProductRequest request = new ProductRequest(name, description, price, categoryId, stock);
        String sellerEmail = authentication.getName();
        ProductResponse resp = productService.updateProduct(id, request, file, sellerEmail);
        return ResponseEntity.ok(resp);
    }



    /**
     * Delete a product by its ID.
     *
     * Only accessible by the product owner (SELLER) or ADMIN.
     *
     * @param id the UUID of the product
     * @param authentication the current authenticated user (used to verify owner)
     * @return MessageResponse indicating deletion success
     */    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProduct(
            @PathVariable UUID id,
            Authentication authentication) {

        String sellerEmail = authentication.getName();
        MessageResponse resp = productService.deleteProduct(id, sellerEmail);
        return ResponseEntity.ok(resp);
    }
}
