package com.bookstore.repository.commerce;

import com.bookstore.entity.commerce.Payment;
import com.bookstore.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends
        JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    @Query("SELECT p FROM Payment p WHERE (:status IS NULL OR p.status = :status)")
    Page<Payment> findAllByStatus(@Param(value = "status") PaymentStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM Payment p
        WHERE p.order.user.id = :userId
            AND (:status IS NULL OR p.status = :status)
    """)
    Page<Payment> findAllByUserIdAndStatus(@Param(value = "userId") UUID userId,
                                           @Param(value = "status") PaymentStatus status,
                                           Pageable pageable);

    boolean existsByOrderId(UUID orderId);
}
