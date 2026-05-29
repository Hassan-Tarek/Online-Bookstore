package com.bookstore.controller.catalog;

import com.bookstore.dto.catalog.request.AuthorCreateRequest;
import com.bookstore.dto.catalog.request.AuthorUpdateRequest;
import com.bookstore.dto.catalog.response.AuthorResponse;
import com.bookstore.dto.catalog.response.BookSummaryResponse;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.catalog.AuthorService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/authors",
        produces = "application/json")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<AuthorResponse>> getAllAuthors(
            Pageable pageable) {
        var responses = authorService.getAllAuthors(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthorResponse> getAuthorById(
            @PathVariable UUID id) {
        var response = authorService.getAuthor(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/{id}/books")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<BookSummaryResponse>> getAuthorBooks(
            @PathVariable UUID id,
            Pageable pageable) {
        var response = authorService.getAuthorBooks(id, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/following")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AuthorResponse>> getMyFollowingAuthors(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        var responses = authorService.getMyFollowingAuthors(userDetails.getUser(), pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponse> createAuthor(
            @Valid @RequestPart(value = "data") AuthorCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile profileImage) {
        var response = authorService.createAuthor(request, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(path = "/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthorResponse> followAuthor(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = authorService.followAuthor(id, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/{id}/unfollow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthorResponse> unfollowAuthor(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = authorService.unfollowAuthor(id, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable UUID id,
            @Valid @RequestBody AuthorUpdateRequest request) {
        var response = authorService.updateAuthor(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/{id}/profile-image",
            consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponse> updateAuthorProfileImage(
            @PathVariable UUID id,
            @RequestParam MultipartFile profileImage) {
        var response = authorService.updateAuthorProfileImage(id, profileImage);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/{id}/profile-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuthorProfileImage(
            @PathVariable UUID id) {
        authorService.deleteAuthorProfileImage(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuthor(
            @PathVariable UUID id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
