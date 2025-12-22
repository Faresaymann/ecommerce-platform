package com.example.backend.Product.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.backend.Category.entity.Category;
import com.example.backend.Category.exception.CategoryNotFoundException;
import com.example.backend.Category.repository.CategoryRepository;

import com.example.backend.Product.dto.ProductRequest;
import com.example.backend.Product.dto.ProductResponse;
import com.example.backend.Product.entity.Product;
import com.example.backend.Product.exception.*;
import com.example.backend.Product.repository.ProductRepository;
import com.example.backend.Product.service.ProductService;
import com.example.backend.auth.dto.Responses.MessageResponse;
import com.example.backend.entity.Users;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UsersRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

     private final ProductRepository repo;
     private final CategoryRepository categoryRepo;
     private final Cloudinary cloudinary;
     private final UsersRepo usersRepository;




    /**
     * Create a new product.
     *
     * Validates product name uniqueness, category existence, and sets the authenticated user as seller.
     * Optionally uploads a product image to Cloudinary.
     *
     * @param request the product data
     * @param file optional product image file
     * @param sellerEmail the email of the authenticated seller
     * @return ProductResponse containing created product details
     * @throws IOException if an error occurs while uploading the image
     */
    @Override
    public ProductResponse createProduct(ProductRequest request, MultipartFile file ,String sellerEmail ) throws IOException {
        String name = request.getName().trim();
        if (repo.existsByNameIgnoreCase(name)) {
            throw new ProductAlreadyExistsException("Product with this name already exists.");
        }

        // category validation
        Long categoryId = request.getCategoryId();
        if (categoryId == null) {
            throw new InvalidProductException("Category ID is required");
        }
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id : " + categoryId));

        // owner of the product validation
        Users seller = usersRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", sellerEmail));

        Product product = Product.builder()
                .name(name)
                .description(request.getDescription())
                .price(BigDecimal.valueOf(request.getPrice()))
                .stock(request.getStock())
                .category(category)
                .seller(seller)  // set owner
                .active(true)    // active by default
                .build();

        // Upload Product image to Cloudinary
        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "Products",// folder name
                            "public_id", name,//file name
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            product.setImageUrl(imageUrl);
        }
        repo.save(product);
        return mapToResponse(product, "Product created successfully :<>:");
    }




    /**
     * Get a product by its UUID.
     *
     * Validates that the product is active and in stock.
     *
     * @param id the UUID of the product
     * @return ProductResponse with product details
     * @throws ProductNotFoundException if product does not exist
     * @throws ProductInactiveException if product is inactive
     * @throws ProductOutOfStockException if product stock is zero
     */
    @Override
    public ProductResponse getProductById(UUID id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + id));

        if (Boolean.FALSE.equals(product.getActive())) {
            throw new ProductInactiveException("Product is not active right now.");
        }
        if (product.getStock() == null || product.getStock() <= 0) {
            throw new ProductOutOfStockException("Sorry, product is out of stock.");
        }
        return mapToResponse(product, "Product found successfully :D ");

    }




    /**
     * Get all products.
     *
     * @return list of ProductResponse objects
     */
    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> products = repo.findAll();
        if (products.isEmpty()) {
            return Collections.emptyList();
            //throw new ProductNotFoundException("No Products Found :( ");
        }
        return products.stream().map(p -> mapToResponse(p, "Products found successfully :D ")).collect(Collectors.toList());
    }



    /**
     * Update an existing product.
     *
     * Only allowed for the product owner or admin. Validates updates and optionally uploads a new image.
     *
     * @param id the UUID of the product to update
     * @param request the updated product data
     * @param file optional new product image
     * @param sellerEmail the email of the authenticated user
     * @return ProductResponse containing updated product details
     * @throws IOException if an error occurs while uploading the image
     * @throws ProductOwnershipException if user is not owner/admin
     * @throws ProductNotFoundException if product does not exist
     */    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request , MultipartFile file, String sellerEmail) throws IOException {
        // fetch product or throw
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + id));

        // OWNER check: allow if product.seller.email == sellerEmail OR if user has ADMIN role
        if (!product.getSeller().getEmail().equalsIgnoreCase(sellerEmail) && !isCurrentUserAdmin()) {
            throw new ProductOwnershipException("You are not the owner of this product");
        }

        // update name if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String newName = request.getName().trim();
            if (!product.getName().equalsIgnoreCase(newName) &&
                    repo.existsByNameIgnoreCase(newName)) {
                throw new ProductAlreadyExistsException("Product with this name already exists.");
            }
            product.setName(newName);
        }

        // update description if provided
        // description
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            product.setDescription(request.getDescription().trim());
        }

        // update price if provided
        if (request.getPrice() != null) {
            if (request.getPrice() < 0) {
                throw new InvalidProductException("Price cannot be negative");
            }
            product.setPrice(BigDecimal.valueOf(request.getPrice()));
        }

        // update stock if provided
        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new InvalidProductException("Stock cannot be negative");
            }
            product.setStock(request.getStock());
        }

        // update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "Category not found with id : " + request.getCategoryId()));
            product.setCategory(category);
        }

        // update image if provided
        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "Products",
                            "public_id", product.getName(),
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            product.setImageUrl(imageUrl);
        }

        repo.save(product); // persist changes
        return mapToResponse(product, "Product updated successfully");
    }



    /**
     * Delete a product by UUID.
     *
     * Only allowed for the product owner or admin.
     *
     * @param id the UUID of the product
     * @param sellerEmail the email of the authenticated user
     * @return MessageResponse confirming deletion
     * @throws ProductOwnershipException if user is not owner/admin
     * @throws ProductNotFoundException if product does not exist
     */
    @Override
    public MessageResponse deleteProduct(UUID id , String sellerEmail)  {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + id));

        if (!product.getSeller().getEmail().equalsIgnoreCase(sellerEmail) && !isCurrentUserAdmin()) {
            throw new ProductOwnershipException("You are not allowed to delete this product");
        }
        repo.deleteById(id);
        return new MessageResponse("Product deleted successfully");
    }


    //---------------------------------------------------helper mapper--------------------------------------------------//

    // --- mapToResponse --- //
    private ProductResponse mapToResponse(Product p, String message) {
        Long categoryId = null;
        String categoryName = null;
        if (p.getCategory() != null) {
            categoryId = p.getCategory().getId();
            categoryName = p.getCategory().getName();
        }

        return ProductResponse.builder()
                .message(message)
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .stock(p.getStock())
                .active(p.getActive())
                .imageUrl(p.getImageUrl())
                .build();
    }


    // --- isCurrentUserAdmin --- //
    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
