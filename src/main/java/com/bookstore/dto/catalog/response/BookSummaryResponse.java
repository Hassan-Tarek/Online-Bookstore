package com.bookstore.dto.catalog.response;

import com.bookstore.enums.InventoryStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record BookSummaryResponse(
        UUID id,
        String title,
        BigDecimal price,
        String coverImageUrl,
        Long ratingCount,
        Double averageRating,
        InventoryStatus stockStatus
) { }
