package com.bookstore.dto.user.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WishlistItemRequest(
        @NotNull UUID bookId
) { }
