package com.bookstore.mapper.notification;

import com.bookstore.dto.notification.request.NotificationCreateRequest;
import com.bookstore.dto.notification.response.NotificationResponse;
import com.bookstore.entity.notification.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);

    Notification toEntity(NotificationCreateRequest request);
}
