package com.bookstore.controller.commerce;

import com.bookstore.dto.commerce.request.CartItemCreateRequest;
import com.bookstore.dto.commerce.request.CartItemUpdateRequest;
import com.bookstore.dto.commerce.response.CartResponse;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.commerce.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping(path = "/api/v1/cart",
        produces = "application/json")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = cartService.getCartByUser(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/items")
    public ResponseEntity<CartResponse> addCartItem(
            @Valid @RequestBody CartItemCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = cartService.addCartItem(request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(path = "/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody CartItemUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = cartService.updateCartItem(itemId, request, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/items/{itemId}")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.deleteCartItem(itemId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clearCart(userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
