package com.bookstore.dto.catalog.response;

import com.bookstore.enums.InventoryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID bookId,
        Integer quantity,
        Integer reserved,
        Integer availableStock,
        InventoryStatus status,
        LocalDateTime createdAt
) { }
