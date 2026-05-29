package com.bookstore.repository.catalog;

import com.bookstore.entity.catalog.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends
        JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByBookId(UUID bookId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.id = :id")
    Optional<Inventory> findByIdForUpdate(@Param("id") UUID id);
}
