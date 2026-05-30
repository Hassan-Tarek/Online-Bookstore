package com.bookstore.repository.commerce;

import com.bookstore.entity.commerce.Promotion;
import com.bookstore.enums.PromotionScope;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends
        JpaRepository<Promotion, UUID> {

    @Query("SELECT p FROM Promotion p WHERE :scope IS NULL OR p.scope = :scope")
    Page<Promotion> findAllByScope(@Param("scope") PromotionScope scope, Pageable pageable);

    @Query("""
        SELECT DISTINCT p
        FROM Promotion p
        WHERE (:scope IS NULL OR p.scope = :scope)
            AND p.active = TRUE
            AND (CURRENT_DATE BETWEEN p.startDate AND p.endDate)
    """)
    Page<Promotion> findAllActiveByScope(@Param("scope") PromotionScope scope, Pageable pageable);

    Optional<Promotion> findByCode(String code);

    @Query("""
        SELECT p
        FROM Promotion p
        WHERE p.scope = com.bookstore.enums.PromotionScope.BOOK
            AND p.active = TRUE
            AND (CURRENT_DATE BETWEEN p.startDate AND p.endDate)
    """)
    Optional<Promotion> findActiveByBookId(UUID bookId);

    @EntityGraph(attributePaths = { "books" })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Promotion p WHERE p.id = :id")
    Optional<Promotion> findByIdForUpdate(@Param("id") UUID id);
}
