package com.bookstore.dto.commerce.request;

import com.bookstore.enums.PromotionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionUpdateRequest(

        @Size(max = 255, message = "Promotion title must not exceed 255 characters")
        String title,

        @Size(max = 50, message = "Promotion code must not exceed 50 characters")
        String code,

        PromotionType type,

        @DecimalMin(value = "0.01", message = "Promotion value must be at least 0.01")
        @DecimalMax(value = "100.00", message = "Promotion value must not exceed 100")
        BigDecimal value,

        @Positive(message = "Usage limit must be greater than 0")
        Integer usageLimit,

        @Min(0)
        BigDecimal minCheckoutAmount,

        LocalDate startDate,

        LocalDate endDate,

        Boolean active
) { }
