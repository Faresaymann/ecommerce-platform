package com.example.backend.Category.service.Impl;

import com.example.backend.Category.dto.CategoryRequest;
import com.example.backend.Category.dto.CategoryResponse;
import com.example.backend.Category.entity.Category;
import com.example.backend.Category.exception.*;
import com.example.backend.Category.repository.CategoryRepository;
import com.example.backend.Category.service.CategoryService;
import com.example.backend.Product.dto.ProductResponse;
import com.example.backend.Product.repository.ProductRepository;
import com.example.backend.auth.dto.Responses.MessageResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;



    /**
     * Create a new category.
     *
     * @param request the {@link CategoryRequest} containing name, description, and optional parentId
     * @return the created {@link CategoryResponse} with a success message
     * @throws CategoryAlreadyExistsException if a category with the same name already exists
     * @throws InvalidCategoryException if parentId is invalid
     */
    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new CategoryAlreadyExistsException("Category with this name already exists.");
        }
        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        // handle parent part
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new InvalidCategoryException("Parent category not found with id " + request.getParentId()));
            category.setParent(parent);
        }
        categoryRepository.save(category);

        return mapToResponse(category, "Category created successfully");
    }



    /**
     * Get a category by its ID.
     *
     * @param id the ID of the category
     * @return the {@link CategoryResponse} for the requested category
     * @throws CategoryNotFoundException if the category does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));
        return mapToResponse(category , "Category found successfully");
    }



    /**
     * Get all root categories and their subcategories recursively.
     *
     * @return a list of {@link CategoryResponse} objects representing root categories
     */    @SuppressWarnings("SimplifyStreamApiCallChains")
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();

        // Filter root categories (parent == null)
        List<Category> rootCategories = allCategories.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.toList());

        // Map each root category to CategoryResponse, including subcategories recursively
        return rootCategories.stream()
                .map(c -> mapToResponse(c, "")) // empty message for listing
                .collect(Collectors.toList());
    }





    /**
     * Update an existing category.
     *
     * @param id the ID of the category to update
     * @param request the {@link CategoryRequest} with updated data
     * @return the updated {@link CategoryResponse}
     * @throws CategoryNotFoundException if category does not exist
     * @throws CategoryUpdateException if the name conflicts with another category
     * @throws InvalidCategoryException if parentId is invalid or causes a cycle
     */    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));

        // handle name update with uniqueness check (exclude current id)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String newName = request.getName().trim();
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
                throw new CategoryUpdateException("Category with this name already exists.");
            }
            category.setName(newName);
        }

        // update description
        category.setDescription(request.getDescription());

        // handle parent update (can be null to remove parent)
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new InvalidCategoryException("Category cannot be its own parent.");
            }
            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new InvalidCategoryException("Parent category not found with id " + request.getParentId()));

            // ensure 'category' is not an ancestor of the newParent
            Category cur = newParent;
            while (cur != null) {
                if (cur.getId().equals(category.getId())) {
                    throw new InvalidCategoryException("Setting this parent would create a cycle.");
                }
                cur = cur.getParent();
            }

            category.setParent(newParent);
        } else {
            // request explicitly null => make it a root category
            category.setParent(null);
        }

        categoryRepository.save(category);

        return mapToResponse(category, "Category updated successfully");

    }




    /**
     * Delete a category by ID.
     *
     * @param id the ID of the category to delete
     * @return a {@link MessageResponse} confirming deletion
     * @throws CategoryNotFoundException if category does not exist
     * @throws CategoryDeletionException if category has subcategories or linked products
     */    @Override
    public MessageResponse deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));

        // If subcategories exist -> prevent delete
        boolean hasSub = category.getSubCategories() != null && !category.getSubCategories().isEmpty();
        if (hasSub) {
            throw new CategoryDeletionException("Cannot delete category that has subcategories.");
        }

        // Check for linked products
        if (productRepository.existsByCategory(category)) {
            throw new CategoryDeletionException("Cannot delete category that has products linked to it.");
        }

        categoryRepository.deleteById(id);
        return new MessageResponse("Category deleted successfully");
    }




    /**
     * Map a Category entity to {@link CategoryResponse}, including subcategories and products.
     *
     * @param c the category entity
     * @param message optional message
     * @return the {@link CategoryResponse} representing the category
     */
    private CategoryResponse mapToResponse(Category c, String message) {
        Long parentId = c.getParent() != null ? c.getParent().getId() : null;
        String parentName = c.getParent() != null ? c.getParent().getName() : null;

        // Recursively map subcategories
        List<CategoryResponse> subCat = (c.getSubCategories() == null ? Collections.emptyList() : c.getSubCategories())
                .stream()
                .map(sub -> mapToResponse((Category) sub, ""))
                .collect(Collectors.toList());

        List<ProductResponse> products = c.getProducts() != null
                ? c.getProducts().stream()
                .map(p -> ProductResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .price(p.getPrice())
                        .stock(p.getStock())
                        .active(p.getActive())
                        .imageUrl(p.getImageUrl())
                        .categoryId(c.getId())
                        .categoryName(c.getName())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return CategoryResponse.builder()
                .message(message)
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .parentId(parentId)
                .parentName(parentName)
                .subCategories(subCat)
                .products(products)
                .build();
    }
}
