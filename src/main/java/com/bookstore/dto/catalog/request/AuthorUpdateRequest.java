package com.bookstore.dto.catalog.request;

public record AuthorUpdateRequest(
        String name,
        String biography
) { }
