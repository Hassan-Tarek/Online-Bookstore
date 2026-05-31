package com.bookstore.repository.commerce;

import com.bookstore.entity.commerce.Order;
import com.bookstore.enums.OrderStatus;
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
}
