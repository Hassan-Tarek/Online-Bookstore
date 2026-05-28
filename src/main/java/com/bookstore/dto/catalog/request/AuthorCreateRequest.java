package com.bookstore.dto.catalog.request;

import jakarta.validation.constraints.NotBlank;

public record AuthorCreateRequest(
        @NotBlank String name,
        String biography
) { }
