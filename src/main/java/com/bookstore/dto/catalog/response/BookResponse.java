package com.bookstore.dto.catalog.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String isbn,
        String title,
        Integer printLength,
        String language,
        String description,
        BigDecimal price,
        Date publicationDate,
        String coverImageUrl,
        Long ratingCount,
        Double averageRating,
        Integer availableStock,
        Integer seriesOrder,
        SeriesResponse series,
        Set<CategoryResponse> categories,
        Set<AuthorResponse> authors,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
