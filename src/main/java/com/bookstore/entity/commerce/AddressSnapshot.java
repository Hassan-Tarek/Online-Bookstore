package com.bookstore.entity.commerce;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressSnapshot {
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
}
