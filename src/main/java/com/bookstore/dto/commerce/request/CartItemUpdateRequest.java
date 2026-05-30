package com.bookstore.dto.commerce.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemUpdateRequest(
        @NotNull @Positive Integer quantity
) { }
