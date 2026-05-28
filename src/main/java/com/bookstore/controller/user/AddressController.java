package com.bookstore.controller.user;

import com.bookstore.dto.user.request.AddressCreateRequest;
import com.bookstore.dto.user.request.AddressUpdateRequest;
import com.bookstore.dto.user.response.AddressResponse;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.user.AddressService;
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
@RequestMapping(path = "/api/v1/addresses",
        produces = "application/json")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<Page<AddressResponse>> getMyAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        var responses = addressService.getAddressesByUser(userDetails.getUser(), pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<AddressResponse> getAddressById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = addressService.getAddress(id, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody AddressCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = addressService.createAddress(request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = addressService.updateAddress(id, request, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = addressService.setDefaultAddress(id, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        addressService.deleteAddress(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
