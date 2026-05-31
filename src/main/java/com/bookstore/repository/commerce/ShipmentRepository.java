package com.bookstore.repository.commerce;

import com.bookstore.entity.commerce.Shipment;
import com.bookstore.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ShipmentRepository extends
        JpaRepository<Shipment, UUID> {

    @Query("SELECT s FROM Shipment s WHERE (:status IS NULL OR s.status = :status)")
    Page<Shipment> findAllByStatus(ShipmentStatus status, Pageable pageable);

    @Query("""
        SELECT s FROM Shipment s
        WHERE s.order.user.id = :userId
            AND (:status IS NULL OR s.status = :status)
    """)
    Page<Shipment> findAllByUserIdAndStatus(@Param(value = "userId") UUID userId,
                                            @Param(value = "status") ShipmentStatus status,
                                            Pageable pageable);

    boolean existsByOrderId(UUID orderId);
}
