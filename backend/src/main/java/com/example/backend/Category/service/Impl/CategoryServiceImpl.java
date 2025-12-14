package com.example.backend.Category.service.Impl;

import com.example.backend.Category.dto.CategoryRequest;
import com.example.backend.Category.dto.CategoryResponse;
import com.example.backend.Category.entity.Category;
import com.example.backend.Category.exception.*;
import com.example.backend.Category.repository.CategoryRepository;
import com.example.backend.Category.service.CategoryService;
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

    //---------------------------------------------------createCategory--------------------------------------------------//
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

    //---------------------------------------------------getCategoryById--------------------------------------------------//
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));
        return mapToResponse(category , "Category found successfully");
    }

    //---------------------------------------------------getAllCategories--------------------------------------------------//
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
    //---------------------------------------------------updateCategory--------------------------------------------------//
    @Override
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
    //---------------------------------------------------deleteCategory--------------------------------------------------//
    @Override
    public MessageResponse deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));

        // If subcategories exist -> prevent delete
        boolean hasSub = category.getSubCategories() != null && !category.getSubCategories().isEmpty();
        if (hasSub) {
            throw new CategoryDeletionException("Cannot delete category that has subcategories.");
        }

        // TODO: check for linked products before delete here
        categoryRepository.deleteById(id);
        return new MessageResponse("Category deleted successfully");
    }

    //---------------------------------------------------mapToResponse--------------------------------------------------//

    // --- helper mapper --- //
    private CategoryResponse mapToResponse(Category c, String message) {
        Long parentId = c.getParent() != null ? c.getParent().getId() : null;
        String parentName = c.getParent() != null ? c.getParent().getName() : null;

        // Recursively map subcategories
        List<CategoryResponse> subCat = (c.getSubCategories() == null ? Collections.emptyList() : c.getSubCategories())
                .stream()
                .map(sub -> mapToResponse((Category) sub, ""))
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .message(message)
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .parentId(parentId)
                .parentName(parentName)
                .subCategories(subCat)
                .build();
    }
}
