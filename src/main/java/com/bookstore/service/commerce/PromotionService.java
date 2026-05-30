package com.bookstore.service.commerce;

import com.bookstore.dto.commerce.request.PromotionCreateRequest;
import com.bookstore.dto.commerce.request.PromotionUpdateRequest;
import com.bookstore.dto.commerce.response.PromotionResponse;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.commerce.Promotion;
import com.bookstore.enums.PromotionScope;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.commerce.PromotionMapper;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.commerce.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final BookRepository bookRepository;
    private final PromotionMapper promotionMapper;

    public Page<PromotionResponse> getAllPromotions(PromotionScope scope, Pageable pageable) {
        return promotionRepository.findAllByScope(scope, pageable)
                .map(promotionMapper::toResponse);
    }

    public Page<PromotionResponse> getAllActivePromotions(PromotionScope scope, Pageable pageable) {
        return promotionRepository.findAllActiveByScope(scope, pageable)
                .map(promotionMapper::toResponse);
    }

    public Optional<PromotionResponse> getActivePromotionByBookId(UUID bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book with id " + bookId + " not found");
        }
        return promotionRepository.findActiveByBookId(bookId)
                .map(promotionMapper::toResponse);
    }

    public PromotionResponse getPromotionById(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion with id " + id + " not found"));
        return promotionMapper.toResponse(promotion);
    }

    public PromotionResponse getPromotionByCode(String code) {
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion with code " + code + " not found"));
        return promotionMapper.toResponse(promotion);
    }

    public PromotionResponse createPromotion(PromotionCreateRequest request) {
        Promotion promotion = promotionMapper.toEntity(request);
        promotionRepository.save(promotion);
        return promotionMapper.toResponse(promotion);
    }

    public PromotionResponse applyPromotionToBook(UUID promotionId, UUID bookId) {
        Promotion promotion = promotionRepository.findByIdForUpdate(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion with id " + promotionId + " not found"));

        validateBookPromotion(promotion);

        boolean alreadyApplied = promotion.getBooks()
                .stream()
                .anyMatch(book -> book.getId().equals(bookId));
        if (alreadyApplied) {
            throw new ConflictException("Promotion is already applied to book with id " + bookId);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + bookId + " not found"));
        promotion.getBooks().add(book);
        promotionRepository.save(promotion);
        return promotionMapper.toResponse(promotion);
    }

    public PromotionResponse updatePromotion(UUID id, PromotionUpdateRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion with id " + id + " not found"));
        promotionMapper.updateEntity(request, promotion);
        promotionRepository.save(promotion);
        return promotionMapper.toResponse(promotion);
    }

    public PromotionResponse activatePromotion(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion with id " + id + " not found"));
        promotion.setActive(true);
        promotionRepository.save(promotion);
        return promotionMapper.toResponse(promotion);
    }

    public PromotionResponse deactivatePromotion(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion with id " + id + " not found"));
        promotion.setActive(false);
        promotionRepository.save(promotion);
        return promotionMapper.toResponse(promotion);
    }

    public void removePromotionFromBook(UUID promotionId, UUID bookId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion with id " + promotionId + " not found"));
        promotion.getBooks().removeIf(book -> book.getId().equals(bookId));
        promotionRepository.save(promotion);
    }

    public void deletePromotion(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion with id " + id + " not found"));
        promotionRepository.delete(promotion);
    }

    private void validatePromotion(Promotion promotion) {
        if (!promotion.getActive()) {
            throw new BadRequestException("Promotion is not active");
        }

        if (promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new BadRequestException("Promotion usage limit exceeded");
        }

        LocalDate now = LocalDate.now();
        if (now.isBefore(promotion.getStartDate())) {
            throw new BadRequestException("Promotion has not started yet");
        }
        if (now.isAfter(promotion.getEndDate())) {
            throw new BadRequestException("Promotion has expired");
        }
    }

    private void validateBookPromotion(Promotion promotion) {
        validatePromotion(promotion);

        if (!promotion.getScope().equals(PromotionScope.BOOK)) {
            throw new BadRequestException("Promotion scope is not BOOK");
        }
    }

    public void validateOrderPromotion(Promotion promotion, BigDecimal subTotal) {
        validatePromotion(promotion);

        if (promotion.getScope() != PromotionScope.GLOBAL) {
            throw new BadRequestException("This is not a global order promotion");
        }

        if (subTotal.compareTo(promotion.getMinCheckoutAmount()) < 0) {
            throw new BadRequestException("Minimum checkout amount of " + promotion.getMinCheckoutAmount() + " not met");
        }
    }
}
