package com.bookstore.repository.commerce.projection;

import java.math.BigDecimal;

public interface TopCustomerProjection {
    String getFirstName();
    String getLastName();
    String getEmail();
    Long getTotalOrders();
    BigDecimal getTotalSpent();
}
