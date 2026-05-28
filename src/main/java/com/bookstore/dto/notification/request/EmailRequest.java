package com.bookstore.dto.notification.request;

public record EmailRequest(
        String to,
        String subject,
        String body
) { }
