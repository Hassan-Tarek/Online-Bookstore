package com.bookstore.controller.notification;

import com.bookstore.dto.notification.response.NotificationResponse;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/notifications",
        produces = "application/json")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        var responses = notificationService.getAllNotifications(userDetails.getUser(), pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        var responses = notificationService.getUnreadNotifications(userDetails.getUser(), pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/read")
    public ResponseEntity<Page<NotificationResponse>> getReadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        var responses = notificationService.getReadNotifications(userDetails.getUser(), pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = notificationService.getNotificationById(id, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markNotificationAsRead(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/read-all")
    public ResponseEntity<Void> markAllNotificationAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllNotificationsAsRead(userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.deleteNotification(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
