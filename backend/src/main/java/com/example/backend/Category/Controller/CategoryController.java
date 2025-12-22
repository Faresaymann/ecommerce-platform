package com.example.backend.Category.Controller;

import com.example.backend.Category.dto.CategoryRequest;
import com.example.backend.Category.dto.CategoryResponse;
import com.example.backend.Category.service.CategoryService;
import com.example.backend.auth.dto.Responses.MessageResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    /**
     * Create a new category (ADMIN only).
     *
     * @param categoryRequest the category data to create
     * @return the created {@link CategoryResponse}
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest categoryRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryRequest));
    }

    /**
     * Get a category by its ID.
     *
     * @param id the ID of the category to retrieve
     * @return the {@link CategoryResponse} for the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }


    /**
     * Get all categories.
     *
     * @return a list of all {@link CategoryResponse} objects
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Update an existing category (ADMIN only).
     *
     * @param id the ID of the category to update
     * @param categoryRequest the new category data
     * @return the updated {@link CategoryResponse}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id , @RequestBody @Valid CategoryRequest categoryRequest){
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryRequest));
    }


    /**
     * Delete a category by ID (ADMIN only).
     *
     * @param id the ID of the category to delete
     * @return a {@link MessageResponse} confirming deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }
}
