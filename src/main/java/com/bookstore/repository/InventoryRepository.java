package com.bookstore.repository;

import com.bookstore.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryRepository extends
        JpaRepository<Inventory, UUID> {
}
