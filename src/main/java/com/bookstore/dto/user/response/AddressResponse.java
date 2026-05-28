package com.bookstore.dto.user.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        String street,
        String city,
        String state,
        String country,
        String zipCode,
        Boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
