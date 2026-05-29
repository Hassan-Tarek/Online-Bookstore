package com.bookstore.dto.catalog.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookSearchCriteria(
        String title,
        String series,
        String category,
        String author,
        String language,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minRating,
        Integer maxRating,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate publicationDate
) { }
