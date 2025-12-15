package com.example.backend.Product.repository;

import com.example.backend.Category.entity.Category;
import com.example.backend.Product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByNameIgnoreCase(String name);

    // check if their is a product in the category (for category deletion)
    boolean existsByCategory(Category category);

}
