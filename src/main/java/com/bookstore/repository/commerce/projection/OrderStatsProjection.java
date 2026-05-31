package com.bookstore.repository.commerce.projection;

import java.math.BigDecimal;

public interface OrderStatsProjection {
    Long getTotalOrders();
    BigDecimal getTotalRevenue();
    BigDecimal getAverageOrderValue();
}
