package com.bookstore.dto.catalog.response;


import com.bookstore.dto.user.response.UserSummaryResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        Integer rating,
        String title,
        String content,
        UserSummaryResponse user,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
