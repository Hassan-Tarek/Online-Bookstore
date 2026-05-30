package com.bookstore.dto.commerce.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CartItemCreateRequest(
        @NotNull UUID bookId,
        @Positive Integer quantity
) { }
