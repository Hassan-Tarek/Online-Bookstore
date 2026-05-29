package com.bookstore.dto.catalog.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public record BookUpdateRequest(
        String isbn,
        String title,
        @Positive Integer printLength,
        String language,
        String description,
        @DecimalMin("0.0") BigDecimal price,
        String publisher,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Date publicationDate,
        UUID seriesId,
        Integer seriesOrder,
        Set<UUID> categoryIds,
        Set<UUID> authorIds
) { }
