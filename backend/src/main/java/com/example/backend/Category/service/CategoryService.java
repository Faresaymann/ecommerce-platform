package com.example.backend.Category.service;

import com.example.backend.Category.dto.CategoryRequest;
import com.example.backend.Category.dto.CategoryResponse;
import com.example.backend.auth.dto.Responses.MessageResponse;

import java.util.List;


public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse getCategoryById(Long id);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    MessageResponse deleteCategory(Long id);
}
