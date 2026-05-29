package com.bookstore.controller.catalog;

import com.bookstore.dto.catalog.request.CategoryCreateRequest;
import com.bookstore.dto.catalog.request.CategoryUpdateRequest;
import com.bookstore.dto.catalog.response.CategoryResponse;
import com.bookstore.service.catalog.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/categories",
        produces = "application/json")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            Pageable pageable) {
        var responses = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable UUID id) {
        var response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        var response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        var response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }
}
