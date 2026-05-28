package com.bookstore.entity.catalog;

import com.bookstore.enums.InventoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "book_id")
        }
)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Builder.Default
    @Column(name = "available_stock", nullable = false)
    private Integer availableStock = 0;

    @Builder.Default
    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock = 0;

//    @Generated
//    @Column(name = "available_stock", insertable = false, updatable = false)
//    private Integer availableStock;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InventoryStatus status = InventoryStatus.OUT_OF_STOCK;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
}
