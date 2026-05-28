package com.bookstore.dto.user.response;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String firstName,
        String lastName,
        String profileImageUrl
) { }
