package com.bookstore.dto.user.request;

public record AddressUpdateRequest(
        String street,
        String city,
        String state,
        String country,
        String zipCode
) { }
