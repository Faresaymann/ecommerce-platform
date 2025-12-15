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

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

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

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Update product (seller only) — only owner or admin allowed (service will enforce)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    // Delete product (seller only) — only owner or admin allowed (service will enforce)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProduct(
            @PathVariable UUID id,
            Authentication authentication) {

        String sellerEmail = authentication.getName();
        MessageResponse resp = productService.deleteProduct(id, sellerEmail);
        return ResponseEntity.ok(resp);
    }
}
