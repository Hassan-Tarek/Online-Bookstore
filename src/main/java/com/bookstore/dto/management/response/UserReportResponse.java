package com.bookstore.dto.management.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReportResponse {
    private String firstName;
    private String lastName;
    private String email;
    private Long totalOrders;
    private BigDecimal totalSpent;
}
