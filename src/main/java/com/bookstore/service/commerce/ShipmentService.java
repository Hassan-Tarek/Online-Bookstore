package com.bookstore.service.commerce;

import com.bookstore.dto.commerce.request.ShipmentCreateRequest;
import com.bookstore.dto.commerce.response.ShipmentResponse;
import com.bookstore.dto.notification.request.NotificationCreateRequest;
import com.bookstore.entity.commerce.Order;
import com.bookstore.entity.commerce.Shipment;
import com.bookstore.entity.user.User;
import com.bookstore.enums.OrderStatus;
import com.bookstore.enums.ShipmentStatus;
import com.bookstore.exception.AccessDeniedException;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.commerce.ShipmentMapper;
import com.bookstore.repository.commerce.OrderRepository;
import com.bookstore.repository.commerce.ShipmentRepository;
import com.bookstore.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentMapper shipmentMapper;
    private final NotificationService notificationService;

    public Page<ShipmentResponse> getAllShipments(ShipmentStatus status, Pageable pageable) {
        return shipmentRepository.findAllByStatus(status, pageable)
                .map(shipmentMapper::toResponse);
    }

    public Page<ShipmentResponse> getMyShipments(User user, ShipmentStatus status, Pageable pageable) {
        return shipmentRepository.findAllByUserIdAndStatus(user.getId(), status, pageable)
                .map(shipmentMapper::toResponse);
    }

    public ShipmentResponse getShipmentById(UUID id, User user) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShipmentType with id " + id + " not found"));
        if (!shipment.getOrder().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view this shipment.");
        }
        return shipmentMapper.toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse createShipment(ShipmentCreateRequest request) {
        if (shipmentRepository.existsByOrderId(request.orderId())) {
            throw new BadRequestException("Shipment already exists for this order.");
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + request.orderId() + " not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new BadRequestException("Order must be PAID before creating a shipment.");
        }

        Shipment shipment = shipmentMapper.toEntity(request);
        shipment.setOrder(order);
        shipmentRepository.save(shipment);
        return shipmentMapper.toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse updateShipmentStatus(UUID id, ShipmentStatus status) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment with id " + id + " not found"));

        validateShipmentStatus(shipment.getStatus(), status);
        Order order = shipment.getOrder();

        switch (status) {
            case SHIPPED -> {
                order.setStatus(OrderStatus.SHIPPED);
                shipment.setShippedAt(LocalDateTime.now());

                notificationService.createNotification(
                        new NotificationCreateRequest(
                                "Your Order Has Shipped!",
                                String.format("Great news! Your order #%s has been handed over to our courier. Tracking Number: %s.",
                                        order.getId(), shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "N/A")
                        ), order.getUser()
                );
            }
            case DELIVERED -> {
                order.setStatus(OrderStatus.COMPLETED);
                shipment.setDeliveredAt(LocalDateTime.now());

                notificationService.createNotification(
                        new NotificationCreateRequest(
                                "Order Delivered",
                                String.format("Your package for order #%s has been successfully delivered. Enjoy your books!", order.getId())
                        ), order.getUser()
                );
            }
        }

        shipment.setStatus(status);
        shipmentRepository.save(shipment);
        return shipmentMapper.toResponse(shipment);
    }

    private void validateShipmentStatus(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new BadRequestException("ShipmentType is already in status: " + newStatus);
        }

        if (currentStatus == ShipmentStatus.DELIVERED || newStatus.ordinal() < currentStatus.ordinal()) {
            throw new BadRequestException("Invalid shipment status from " + currentStatus + " to " + newStatus);
        }
    }
}
