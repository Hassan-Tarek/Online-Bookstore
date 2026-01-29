package com.bookstore.repository;

import com.bookstore.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PromotionRepository
        extends JpaRepository<Promotion, UUID> {
}
