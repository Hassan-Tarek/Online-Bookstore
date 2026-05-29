package com.bookstore.dto.catalog.request;

public record CategoryUpdateRequest(
        String name,
        String description
) { }
