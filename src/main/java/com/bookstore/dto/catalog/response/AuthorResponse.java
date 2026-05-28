package com.bookstore.dto.catalog.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuthorResponse(
        UUID id,
        String name,
        String biography,
        String profileImageUrl,
        Long followersCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
