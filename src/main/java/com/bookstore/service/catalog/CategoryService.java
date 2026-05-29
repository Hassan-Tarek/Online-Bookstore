package com.bookstore.service.catalog;

import com.bookstore.dto.catalog.request.CategoryCreateRequest;
import com.bookstore.dto.catalog.request.CategoryUpdateRequest;
import com.bookstore.dto.catalog.response.CategoryResponse;
import com.bookstore.entity.catalog.Category;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.catalog.CategoryMapper;
import com.bookstore.repository.catalog.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found"));
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Category category = categoryMapper.toEntity(request);
        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found"));
        categoryMapper.updateEntity(request, category);
        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }
}
