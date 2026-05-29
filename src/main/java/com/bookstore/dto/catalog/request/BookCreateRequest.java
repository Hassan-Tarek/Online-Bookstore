package com.bookstore.dto.catalog.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public record BookCreateRequest(
        @NotBlank String isbn,
        @NotBlank String title,
        @NotNull @Positive Integer printLength,
        @NotNull String language,
        String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotBlank String publisher,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @NotNull Date publicationDate,
        Integer seriesOrder,
        UUID seriesId,
        @NotNull Set<UUID> categoryIds,
        @NotNull Set<UUID> authorIds
) { }
