package com.bookstore.dto.catalog.request;

import jakarta.validation.constraints.NotBlank;

public record SeriesCreateRequest(
        @NotBlank String title,
        String description
) { }
