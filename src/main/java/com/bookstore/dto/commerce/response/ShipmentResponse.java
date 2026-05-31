package com.bookstore.dto.commerce.response;

import com.bookstore.enums.ShipmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        UUID orderId,
        String carrier,
        String trackingNumber,
        ShipmentStatus status,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
