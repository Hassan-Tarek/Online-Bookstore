package com.bookstore.controller.catalog;

import com.bookstore.dto.catalog.request.BookCreateRequest;
import com.bookstore.dto.catalog.request.BookSearchCriteria;
import com.bookstore.dto.catalog.request.BookUpdateRequest;
import com.bookstore.dto.catalog.response.BookResponse;
import com.bookstore.dto.catalog.response.BookSummaryResponse;
import com.bookstore.service.catalog.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/books",
        produces = "application/json")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping(path = "/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<BookSummaryResponse>> searchBooks(
            BookSearchCriteria criteria,
            Pageable pageable) {
        var responses = bookService.searchBooks(criteria, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/top-rated")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<BookSummaryResponse>> getTopRatedBooks(
            Pageable pageable) {
        var responses = bookService.getTopRatedBooks(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/new-releases")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<BookSummaryResponse>> getNewReleaseBooks(
            Pageable pageable) {
        var responses = bookService.getNewReleases(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable UUID id) {
        var response = bookService.getBookById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestPart(value = "data") BookCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile coverImage) {
        var response = bookService.createBook(request, coverImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable UUID id,
            @Valid @RequestBody BookUpdateRequest request) {
        var response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/{id}/cover-image",
            consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> updateBookCoverImage(
            @PathVariable UUID id,
            @RequestParam MultipartFile coverImage) {
        var response = bookService.updateBookCoverImage(id, coverImage);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/{id}/cover-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBookCoverImage(
            @PathVariable UUID id) {
        bookService.deleteBookCoverImage(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(
            @PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
