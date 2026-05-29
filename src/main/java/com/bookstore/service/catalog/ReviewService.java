package com.bookstore.service.catalog;

import com.bookstore.dto.catalog.request.ReviewCreateRequest;
import com.bookstore.dto.catalog.request.ReviewUpdateRequest;
import com.bookstore.dto.catalog.response.ReviewResponse;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.catalog.Review;
import com.bookstore.entity.user.User;
import com.bookstore.enums.Role;
import com.bookstore.exception.AccessDeniedException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.catalog.ReviewMapper;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.catalog.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(UUID bookId, Pageable pageable) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book with id " + bookId + " not found");
        }
        return reviewRepository.findByBookId(bookId, pageable)
                .map(reviewMapper::toResponse);
    }

    @Transactional
    public ReviewResponse createReview(UUID bookId, ReviewCreateRequest request, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + bookId + " not found"));
        Review review = reviewMapper.toEntity(request);
        review.setUser(user);
        review.setBook(book);
        review = reviewRepository.save(review);
        bookRepository.incrementRating(bookId, review.getRating());
        return reviewMapper.toResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, ReviewUpdateRequest request, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review with id " + reviewId + " not found"));

        if (!user.getRole().equals(Role.ADMIN) && !review.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only edit your own reviews");
        }

        reviewMapper.updateEntity(request, review);
        review = reviewRepository.save(review);
        if (!review.getRating().equals(request.rating())) {
            bookRepository.updateRating(review.getBook().getId(), review.getRating(), request.rating());
        }
        return reviewMapper.toResponse(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review with id " + reviewId + " not found"));

        if (!user.getRole().equals(Role.ADMIN) && !review.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
        bookRepository.decrementRating(review.getBook().getId(), review.getRating());
    }
}
