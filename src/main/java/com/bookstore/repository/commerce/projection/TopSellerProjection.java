package com.bookstore.repository.commerce.projection;

import java.math.BigDecimal;

public interface TopSellerProjection {
    String getIsbn();
    String getTitle();
    Long getTotalQuantitySold();
    BigDecimal getTotalRevenue();
}