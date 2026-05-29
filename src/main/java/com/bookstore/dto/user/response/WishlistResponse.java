package com.bookstore.dto.user.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WishlistResponse(
        UUID id,
        List<WishlistItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }

