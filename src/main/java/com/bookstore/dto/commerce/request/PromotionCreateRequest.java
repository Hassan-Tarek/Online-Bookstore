package com.bookstore.dto.commerce.request;

import com.bookstore.enums.PromotionScope;
import com.bookstore.enums.PromotionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionCreateRequest(

        @NotBlank(message = "Promotion title must not be blank")
        @Size(max = 255, message = "Promotion title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Promotion code must not be blank")
        @Size(max = 50, message = "Promotion code must not exceed 50 characters")
        String code,

        @NotNull(message = "Promotion type is required")
        PromotionType type,

        @NotNull(message = "Promotion scope is required")
        PromotionScope scope,

        @NotNull(message = "Promotion value is required")
        @DecimalMin(value = "0.01", message = "Promotion value must be at least 0.01")
        @DecimalMax(value = "100.00", message = "Promotion value must not exceed 100")
        BigDecimal value,

        @NotNull(message = "Usage limit is required")
        @Positive(message = "Usage limit must be greater than 0")
        Integer usageLimit,

        @Min(0)
        BigDecimal minCheckoutAmount,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        Boolean active
) { }
