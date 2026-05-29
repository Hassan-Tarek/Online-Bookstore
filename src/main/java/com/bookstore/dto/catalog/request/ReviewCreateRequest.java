package com.bookstore.dto.catalog.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        String title,
        String content
) { }
