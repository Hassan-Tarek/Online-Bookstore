package com.bookstore.service.notification;

import com.bookstore.dto.notification.request.NotificationCreateRequest;
import com.bookstore.dto.notification.response.NotificationResponse;
import com.bookstore.entity.notification.Notification;
import com.bookstore.entity.user.User;
import com.bookstore.enums.NotificationStatus;
import com.bookstore.exception.AccessDeniedException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.notification.NotificationMapper;
import com.bookstore.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(User user, Pageable pageable) {
        return notificationRepository.findAllByUserId(user.getId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(User user, Pageable pageable) {
        return notificationRepository.findAllByUserIdAndStatus(user.getId(), NotificationStatus.UNREAD, pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getReadNotifications(User user, Pageable pageable) {
        return notificationRepository.findAllByUserIdAndStatus(user.getId(), NotificationStatus.READ, pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification with id " + notificationId + " not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only view your own notifications");
        }
        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public void createNotification(NotificationCreateRequest request, User user) {
        Notification notification = notificationMapper.toEntity(request);
        notification.setUser(user);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markNotificationAsRead(UUID id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification with id " + id + " not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only mark your own notifications as read");
        }
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllNotificationsAsRead(User user) {
        List<Notification> notifications = notificationRepository.findAllByUserId(user.getId());
        for (Notification notification : notifications) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void deleteNotification(UUID id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification with id " + id + " not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only delete your own notifications");
        }
        notificationRepository.delete(notification);
    }
}
