package com.bookstore.dto.commerce.request;

import com.bookstore.entity.commerce.AddressSnapshot;
import com.bookstore.enums.ShippingMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderCreateRequest(
        @NotNull AddressSnapshot shippingAddress,
        @NotNull AddressSnapshot billingAddress,
        @NotNull ShippingMethod shippingMethod,
        String promoCode
) { }
