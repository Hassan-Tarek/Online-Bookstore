package com.bookstore.controller.catalog;

import com.bookstore.dto.catalog.request.ReviewCreateRequest;
import com.bookstore.dto.catalog.request.ReviewUpdateRequest;
import com.bookstore.dto.catalog.response.ReviewResponse;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.catalog.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1",
        produces = "application/json")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping(path = "/books/{bookId}/reviews")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(
            @PathVariable UUID bookId,
            Pageable pageable) {
        var responses = reviewService.getAllReviews(bookId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping(path = "/books/{bookId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID bookId,
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = reviewService.createReview(bookId, request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(path = "/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable(name = "id") UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = reviewService.updateReview(reviewId, request, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(
            @PathVariable(name = "id") UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
