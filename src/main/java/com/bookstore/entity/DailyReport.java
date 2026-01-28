package com.bookstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "daily_reports",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "date")
        }
)
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Builder.Default
    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "orders_count", nullable = false)
    private Integer ordersCount = 0;

    @Builder.Default
    @Column(name = "new_users_count", nullable = false)
    private Integer newUsersCount = 0;

    @Column(name = "top_selling_books", columnDefinition = "TEXT")
    private String topSellingBooks;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
