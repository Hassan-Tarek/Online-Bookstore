package com.bookstore.dto.management.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookReportResponse {
    private String isbn;
    private String title;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
}
