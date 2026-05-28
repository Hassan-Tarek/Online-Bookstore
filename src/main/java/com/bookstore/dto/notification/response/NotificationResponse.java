package com.bookstore.dto.notification.response;

import com.bookstore.enums.NotificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String message,
        NotificationStatus status,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
