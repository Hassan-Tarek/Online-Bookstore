package com.bookstore.service.catalog;

import com.bookstore.dto.catalog.request.InventoryCreateRequest;
import com.bookstore.dto.catalog.request.InventoryStockUpdateRequest;
import com.bookstore.dto.catalog.response.InventoryResponse;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.catalog.Inventory;
import com.bookstore.enums.InventoryStatus;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.catalog.InventoryMapper;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.catalog.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final BookRepository bookRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable)
                .map(inventoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory with id " + id + " not found"));
        return inventoryMapper.toResponse(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByBookId(UUID bookId) {
        Inventory inventory = inventoryRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for book " + bookId + " not found"));
        return inventoryMapper.toResponse(inventory);
    }

    @Transactional
    public InventoryResponse createInventory(InventoryCreateRequest request) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + request.bookId() + " not found"));
        Inventory inventory = inventoryMapper.toEntity(request);
        inventory.setBook(book);
        if (request.quantity() > 0) {
            inventory.setStatus(InventoryStatus.IN_STOCK);
        }
        inventory = inventoryRepository.save(inventory);
        return inventoryMapper.toResponse(inventory);
    }

    @Transactional
    public InventoryResponse updateStock(UUID id, InventoryStockUpdateRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory with id " + id + " not found"));
        inventory.setAvailableStock(inventory.getAvailableStock() + request.amount());
        if (inventory.getStatus() == InventoryStatus.OUT_OF_STOCK && inventory.getAvailableStock() > 0) {
            inventory.setStatus(InventoryStatus.IN_STOCK);
        }
        inventory = inventoryRepository.save(inventory);
        return inventoryMapper.toResponse(inventory);
    }

    @Transactional
    public void reserveStock(UUID id, Integer quantity) {
        Inventory inventory = inventoryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory with id " + id + " not found"));
        if (!inventory.getStatus().equals(InventoryStatus.IN_STOCK) ||
                inventory.getAvailableStock() < quantity) {
            throw new BadRequestException("Insufficient stock");
        }
        inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
        inventory.setReservedStock(inventory.getReservedStock() + quantity);
        if (inventory.getAvailableStock() == 0) {
            inventory.setStatus(InventoryStatus.OUT_OF_STOCK);
        }
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void restoreStock(UUID id, Integer quantity) {
        Inventory inventory = inventoryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory with id " + id + " not found"));
        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
        inventory.setReservedStock(inventory.getReservedStock() - quantity);
        if (inventory.getAvailableStock() > 0 && inventory.getStatus().equals(InventoryStatus.OUT_OF_STOCK)) {
            inventory.setStatus(InventoryStatus.IN_STOCK);
        }
        inventoryRepository.save(inventory);
    }
}
