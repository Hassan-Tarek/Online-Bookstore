package com.bookstore.service.catalog;

import com.bookstore.entity.catalog.Inventory;
import com.bookstore.enums.InventoryStatus;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.catalog.InventoryMapper;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.catalog.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void reserveStock_reducesAvailableAndIncreasesReserved() {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .availableStock(10)
                .reservedStock(2)
                .status(InventoryStatus.IN_STOCK)
                .build();
        when(inventoryRepository.findByIdForUpdate(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.reserveStock(inventoryId, 3);

        assertThat(inventory.getAvailableStock()).isEqualTo(7);
        assertThat(inventory.getReservedStock()).isEqualTo(5);
        assertThat(inventory.getStatus()).isEqualTo(InventoryStatus.IN_STOCK);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void reserveStock_setsOutOfStockWhenAvailableReachesZero() {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .availableStock(2)
                .reservedStock(0)
                .status(InventoryStatus.IN_STOCK)
                .build();
        when(inventoryRepository.findByIdForUpdate(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.reserveStock(inventoryId, 2);

        assertThat(inventory.getAvailableStock()).isZero();
        assertThat(inventory.getStatus()).isEqualTo(InventoryStatus.OUT_OF_STOCK);
    }

    @Test
    void reserveStock_throwsWhenInsufficientStock() {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .availableStock(1)
                .reservedStock(0)
                .status(InventoryStatus.IN_STOCK)
                .build();
        when(inventoryRepository.findByIdForUpdate(inventoryId)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.reserveStock(inventoryId, 5))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void reserveStock_throwsWhenInventoryNotFound() {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryRepository.findByIdForUpdate(inventoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.reserveStock(inventoryId, 1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void restoreStock_returnsStockAndUpdatesStatus() {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .availableStock(0)
                .reservedStock(4)
                .status(InventoryStatus.OUT_OF_STOCK)
                .build();
        when(inventoryRepository.findByIdForUpdate(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.restoreStock(inventoryId, 2);

        assertThat(inventory.getAvailableStock()).isEqualTo(2);
        assertThat(inventory.getReservedStock()).isEqualTo(2);
        assertThat(inventory.getStatus()).isEqualTo(InventoryStatus.IN_STOCK);
    }
}
