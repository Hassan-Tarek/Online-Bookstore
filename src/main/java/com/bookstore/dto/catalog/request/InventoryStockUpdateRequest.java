package com.bookstore.dto.catalog.request;

import jakarta.validation.constraints.Positive;

public record InventoryStockUpdateRequest(
        @Positive Integer amount
) { }
