package com.bookstore.dto.management.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalOrders;
    private Long totalItems;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
}
