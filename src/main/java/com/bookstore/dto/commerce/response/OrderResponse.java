package com.bookstore.dto.commerce.response;

import com.bookstore.entity.commerce.AddressSnapshot;
import com.bookstore.enums.OrderStatus;
import com.bookstore.enums.ShippingMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        AddressSnapshot billingAddress,
        AddressSnapshot shippingAddress,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalPrice,
        String promoCode,
        ShippingMethod shippingMethod,
        OrderStatus status,
        List<OrderItemResponse> items,
        PaymentResponse payment,
        ShipmentResponse shipment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
