package com.bookstore.dto.catalog.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InventoryCreateRequest(
        @NotNull UUID bookId,
        @NotNull @Min(0) Integer quantity
) { }
