package com.bookstore.dto.commerce.response;

import com.bookstore.dto.catalog.response.BookSummaryResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        BookSummaryResponse book,
        Integer quantity,
        BigDecimal originalUnitPrice,
        BigDecimal finalUnitPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
