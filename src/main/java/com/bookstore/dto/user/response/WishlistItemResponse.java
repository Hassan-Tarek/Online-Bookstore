package com.bookstore.dto.user.response;

import com.bookstore.dto.catalog.response.BookSummaryResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record WishlistItemResponse(
        UUID id,
        BookSummaryResponse book,
        LocalDateTime createdAt
) { }
