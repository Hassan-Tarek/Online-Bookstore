package com.bookstore.controller.user;

import com.bookstore.dto.user.request.WishlistItemRequest;
import com.bookstore.dto.user.response.WishlistResponse;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.user.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/wishlist",
        produces = "application/json")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<WishlistResponse> getMyWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var responses = wishlistService.getMyWishlist(userDetails.getUser());
        return ResponseEntity.ok(responses);
    }

    @PostMapping(path = "/items")
    public ResponseEntity<WishlistResponse> addWishlistItem(
            @Valid @RequestBody WishlistItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = wishlistService.addWishlistItem(request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = "/items/{itemId}")
    public ResponseEntity<Void> deleteWishlistItem(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        wishlistService.deleteWishlistItem(itemId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        wishlistService.clearWishlist(userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
