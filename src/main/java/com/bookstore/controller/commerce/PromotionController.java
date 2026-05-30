package com.bookstore.controller.commerce;

import com.bookstore.dto.commerce.request.PromotionCreateRequest;
import com.bookstore.dto.commerce.request.PromotionUpdateRequest;
import com.bookstore.dto.commerce.response.PromotionResponse;
import com.bookstore.enums.PromotionScope;
import com.bookstore.service.commerce.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/promotions",
        produces = "application/json")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<PromotionResponse>> getAllPromotions(
            @RequestParam(required = false) PromotionScope scope,
            Pageable pageable) {
        var responses = promotionService.getAllPromotions(scope, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/active")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<PromotionResponse>> getAllActivePromotions(
            @RequestParam(required = false) PromotionScope scope,
            Pageable pageable) {
        var responses = promotionService.getAllActivePromotions(scope, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/active/book/{bookId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PromotionResponse> getActivePromotionByBookId(
            @PathVariable UUID bookId) {
        var response = promotionService.getActivePromotionByBookId(bookId);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PromotionResponse> getPromotionById(
            @PathVariable UUID id) {
        var response = promotionService.getPromotionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/code/{code}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PromotionResponse> getPromotionByCode(
            @PathVariable String code) {
        var response = promotionService.getPromotionByCode(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> createPromotion(
            @Valid @RequestBody PromotionCreateRequest request) {
        var response = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(path = "/{id}/books/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> applyPromotionToBook(
            @PathVariable(value = "id") UUID promotionId,
            @PathVariable UUID bookId) {
        var response = promotionService.applyPromotionToBook(promotionId, bookId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> updatePromotion(
            @PathVariable UUID id,
            @Valid @RequestBody PromotionUpdateRequest request) {
        var response = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> activatePromotion(
            @PathVariable UUID id) {
        var response = promotionService.activatePromotion(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> deactivatePromotion(
            @PathVariable UUID id) {
        var response = promotionService.deactivatePromotion(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/{id}/books/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removePromotionFromBook(
            @PathVariable(value = "id") UUID promotionId,
            @PathVariable UUID bookId) {
        promotionService.removePromotionFromBook(promotionId, bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePromotion(
            @PathVariable UUID id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}
