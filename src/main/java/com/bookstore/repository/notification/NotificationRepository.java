package com.bookstore.repository.notification;

import com.bookstore.entity.notification.Notification;
import com.bookstore.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends
        JpaRepository<Notification, UUID> {

    List<Notification> findAllByUserId(UUID userId);

    Page<Notification> findAllByUserId(UUID userId, Pageable pageable);

    Page<Notification> findAllByUserIdAndStatus(UUID userId, NotificationStatus status, Pageable pageable);
}
