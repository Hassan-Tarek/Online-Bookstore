package com.bookstore.dto.catalog.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SeriesResponse(
        UUID id,
        String title,
        String description,
        String coverImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
