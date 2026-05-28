package com.bookstore.dto.user.request;

import jakarta.validation.constraints.NotBlank;

public record AddressCreateRequest(
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String country,
        @NotBlank String zipCode
) { }
