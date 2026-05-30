package com.bookstore.dto.commerce.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        List<CartItemResponse> items,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
