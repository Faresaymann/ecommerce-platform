package com.example.backend.Product.service;

import com.example.backend.Product.dto.ProductRequest;
import com.example.backend.Product.dto.ProductResponse;
import com.example.backend.auth.dto.Responses.MessageResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public interface ProductService {

    ProductResponse createProduct(ProductRequest request, MultipartFile file , String sellerEmail) throws IOException;

    ProductResponse getProductById(UUID id);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(UUID id, ProductRequest request , MultipartFile file , String sellerEmail) throws IOException;

    MessageResponse deleteProduct(UUID id , String sellerEmail);
}
