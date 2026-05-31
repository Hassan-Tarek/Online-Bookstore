package com.bookstore.dto.commerce.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ShipmentCreateRequest(
        @NotNull UUID orderId,
        @NotBlank String carrier,
        @NotBlank String trackingNumber
) { }
