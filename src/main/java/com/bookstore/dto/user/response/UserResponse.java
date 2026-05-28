package com.bookstore.dto.user.response;

import com.bookstore.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        String profileImageUrl,
        Boolean enabled,
        Boolean emailVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
