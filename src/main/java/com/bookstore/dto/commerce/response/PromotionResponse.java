package com.bookstore.dto.commerce.response;

import com.bookstore.enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PromotionResponse(
        UUID id,
        String title,
        String code,
        PromotionType type,
        BigDecimal value,
        LocalDate startDate,
        LocalDate endDate,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
