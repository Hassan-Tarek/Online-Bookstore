package com.bookstore.entity.commerce;

import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.catalog.Category;
import com.bookstore.entity.catalog.Series;
import com.bookstore.enums.PromotionScope;
import com.bookstore.enums.PromotionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "promotions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "code")
        }
)
@SQLDelete(sql = "UPDATE promotions SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction(value = "deleted_at IS NULL")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PromotionType type;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private PromotionScope scope;

    @Column(name = "value", precision = 12, scale = 2, nullable = false)
    private BigDecimal value;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit;

    @Builder.Default
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Builder.Default
    @Column(name = "min_checkout_amount", precision = 12, scale = 2)
    private BigDecimal minCheckoutAmount = BigDecimal.ZERO;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToMany(mappedBy = "promotions", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();
}
