package com.bookstore.controller.catalog;

import com.bookstore.dto.catalog.request.InventoryCreateRequest;
import com.bookstore.dto.catalog.request.InventoryStockUpdateRequest;
import com.bookstore.dto.catalog.response.InventoryResponse;
import com.bookstore.service.catalog.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/inventories",
        produces = "application/json")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<Page<InventoryResponse>> getAllInventories(
            Pageable pageable) {
        var responses = inventoryService.getAllInventories(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<InventoryResponse> getInventoriesById(
            @PathVariable UUID id) {
        var response = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/book/{bookId}")
    public ResponseEntity<InventoryResponse> getInventoryByBookId(
            @PathVariable UUID bookId) {
        var response = inventoryService.getInventoryByBookId(bookId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody InventoryCreateRequest request) {
        var response = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(path = "/{id}/stock")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryStockUpdateRequest request) {
        var response = inventoryService.updateStock(id, request);
        return ResponseEntity.ok(response);
    }
}
