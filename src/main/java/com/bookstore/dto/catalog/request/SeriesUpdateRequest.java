package com.bookstore.dto.catalog.request;

public record SeriesUpdateRequest(
        String title,
        String description
) { }