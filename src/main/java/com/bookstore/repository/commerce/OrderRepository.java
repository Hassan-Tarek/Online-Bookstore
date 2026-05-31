package com.bookstore.repository.commerce;

import com.bookstore.entity.commerce.Order;
import com.bookstore.enums.OrderStatus;
import com.bookstore.repository.commerce.projection.OrderStatsProjection;
import com.bookstore.repository.commerce.projection.TopCustomerProjection;
import com.bookstore.repository.commerce.projection.TopSellerProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends
        JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = { "orderItems", "orderItems.book", "shipment", "payment" })
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = { "orderItems" })
    List<Order> findAllByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoff);

    @Query("""
        SELECT o FROM Order o
        WHERE (:status IS NULL OR o.status = :status)
            AND (:minPrice IS NULL OR o.totalPrice >= :minPrice)
            AND (:maxPrice IS NULL OR o.totalPrice <= :maxPrice)
    """)
    Page<Order> findAllByStatusAndMinPriceAndMaxPrice(
            @Param("status") OrderStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.user.id = :userId
            AND (:status IS NULL OR o.status = :status)
            AND (:minPrice IS NULL OR o.totalPrice >= :minPrice)
            AND (:maxPrice IS NULL OR o.totalPrice <= :maxPrice)
    """)
    Page<Order> findAllByUserIdAndStatusAndMinPriceAndMaxPrice(
            @Param("userId") UUID userId,
            @Param("status") OrderStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT o.id) AS totalOrders,
        COALESCE(SUM(o.totalPrice), 0) AS totalRevenue,
        COALESCE(AVG(o.totalPrice), 0) AS averageOrderValue
        FROM Order o
        WHERE (CAST(:startDate AS LOCALDATETIME) IS NULL OR o.createdAt >= :startDate)
            AND (CAST(:endDate AS LOCALDATETIME) IS NULL OR o.createdAt <= :endDate)
            AND o.status = com.bookstore.enums.OrderStatus.COMPLETED
            AND o.payment.status = com.bookstore.enums.PaymentStatus.PAID
    """)
    OrderStatsProjection findOrderStats(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT COALESCE(SUM(i.quantity), 0)
        FROM OrderItem i
        WHERE (CAST(:startDate AS LOCALDATETIME) IS NULL OR i.order.createdAt >= :startDate)
            AND (CAST(:endDate AS LOCALDATETIME) IS NULL OR i.order.createdAt <= :endDate)
            AND i.order.status = com.bookstore.enums.OrderStatus.COMPLETED
            AND i.order.payment.status = com.bookstore.enums.PaymentStatus.PAID
    """)
    Long findTotalItemsByStartDateAndEndDate(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT b.isbn AS isbn,
        b.title AS title,
        COALESCE(SUM(i.quantity), 0) AS totalQuantitySold,
        COALESCE(SUM(i.quantity * i.finalUnitPrice), 0) AS totalRevenue
        FROM Order o
        JOIN o.orderItems i
        JOIN i.book AS b
        WHERE o.status = com.bookstore.enums.OrderStatus.COMPLETED
            AND o.payment.status = com.bookstore.enums.PaymentStatus.PAID
        GROUP BY b.isbn, b.title
        ORDER BY COALESCE(SUM(i.quantity), 0) DESC,
             COALESCE(SUM(i.finalUnitPrice * i.quantity), 0) DESC
    """)
    Page<TopSellerProjection> findTopSellers(Pageable pageable);

    @Query("""
        SELECT u.firstName AS firstName,
        u.lastName AS lastName,
        u.email AS email,
        COUNT(DISTINCT o.id) AS totalOrders,
        COALESCE(SUM(o.totalPrice), 0) AS totalSpent
        FROM Order o
        JOIN o.user u
        WHERE o.status = com.bookstore.enums.OrderStatus.COMPLETED
            AND o.payment.status = com.bookstore.enums.PaymentStatus.PAID
        GROUP BY u.id, u.firstName, u.lastName, u.email
        ORDER BY SUM(o.totalPrice) DESC
    """)
    Page<TopCustomerProjection> findTopCustomers(Pageable pageable);
}
