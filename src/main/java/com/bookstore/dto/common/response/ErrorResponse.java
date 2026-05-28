package com.bookstore.dto.common.response;

import lombok.Builder;

@Builder
public record ErrorResponse(
        Integer statusCode,
        String message,
        Long timestamp
) { }
