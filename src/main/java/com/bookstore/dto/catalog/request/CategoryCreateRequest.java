package com.bookstore.dto.catalog.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(
        @NotBlank String name,
        String description
) { }
