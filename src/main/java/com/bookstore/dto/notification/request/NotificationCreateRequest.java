package com.bookstore.dto.notification.request;

public record NotificationCreateRequest(
        String title,
        String message
) { }
